package infrastructure.repositories;

import application.AgentCommunicationChanel;
import application.NetworkMessageEmitter;
import application.Queues;
import org.rocksdb.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Map;

public class WordRockRepository implements Runnable {
    AgentCommunicationChanel chanel;
    Queues queues;
    final String NAME = "romulus";
    Integer ownerId;
    File dbPath;
    RocksDB db;

    public WordRockRepository(Queues queues, AgentCommunicationChanel chanel, Integer ownerId) {
        this.queues = queues;
        this.chanel = chanel;
        this.ownerId = ownerId;
    }

    public WordRockRepository() {}

    public void init() {
        RocksDB.loadLibrary();
        final Options options = new Options();
        options.setCreateIfMissing(true);
        options.setMergeOperator(new UInt64AddOperator());
        dbPath = new File("/tmp/rocks-db", NAME);
        try {
            Files.createDirectories(dbPath.getParentFile().toPath());
            Files.createDirectories(dbPath.getAbsoluteFile().toPath());
            db = RocksDB.open(options, dbPath.getAbsolutePath());
            System.out.println("RocksDB initialized and ready to use");
        } catch(IOException | RocksDBException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void run() {
        Long start = System.currentTimeMillis();
        init();

        try (final WriteOptions writeOpt = new WriteOptions()) {

            Map<String, Integer> words;
            while ((words = queues.wordsToPersist.poll()) != null || !chanel.persistenceFinished.get()) {
                if (words != null) {
                        Long insertStart = System.currentTimeMillis();
                        System.out.println("Persisting words into RocksDB");
                        try (final WriteBatch batch = new WriteBatch()) {
                            for (Map.Entry<String, Integer> word : words.entrySet())
                                batch.merge(word.getKey().getBytes(), ByteUtils.longToBytes(word.getValue()));
                            db.write(writeOpt, batch);
                        }
                        catch (RocksDBException e) {
                            throw new RuntimeException(e);
                        }

                        Long insertEnd = System.currentTimeMillis();
                        System.out.printf("Partial Write in %d", insertEnd - insertStart);
                }
            }
        }

        Long end = System.currentTimeMillis();

        System.out.println("RocksDB write completed in " + (end - start) + "ms");

        long max = 0;
        long min = Integer.MAX_VALUE;

        try (RocksIterator iterator = db.newIterator()) {
            for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                long freq = ByteUtils.bytesToLong(iterator.value());
                if (max < freq) max = freq;
                else if (min > freq) min = freq;
            }
        }

        NetworkMessageEmitter.flood(String.format("%d,%d", min,max), this.ownerId);

        System.out.printf("Words min and max: %d ; %d", min, max);

        db.close();
    }

    public RocksIterator getIterator() {
        return db.newIterator();
    }

    public void close() {
        db.close();
    }

    public static class ByteUtils {
        private final static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

        public static byte[] longToBytes(long x) {
            buffer.putLong(0, x);
            return buffer.array();
        }

        public static long bytesToLong(byte[] bytes) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            return buffer.getLong();
        }
    }

}
