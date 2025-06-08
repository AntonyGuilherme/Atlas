package application.consumers.persistence;

import application.communication.Queues;
import application.communication.RunningOptions;
import application.producers.network.MessageProducer;
import org.rocksdb.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Map;

public class ReShuffleRockPersistence implements Runnable {
    RunningOptions options;
    Queues queues;
    final String NAME = "remus";
    Integer ownerId;
    RocksDB db;

    public ReShuffleRockPersistence(Queues queues, RunningOptions options, Integer ownerId) {
        this.queues = queues;
        this.ownerId = ownerId;
        this.options = options;
    }

    public ReShuffleRockPersistence() {}

    public void init() {
        RocksDB.loadLibrary();
        final Options options = new Options();
        options.setCreateIfMissing(true);
        options.setMergeOperator(new UInt64AddOperator());
        File dbPath = new File("db", NAME);
        try {
            Files.createDirectories(dbPath.getParentFile().toPath());
            Files.createDirectories(dbPath.getAbsoluteFile().toPath());
            db = RocksDB.open(options, dbPath.getAbsolutePath());
            System.out.printf("[DATABASE] CREATE %s\n",NAME);
        } catch(IOException | RocksDBException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void run() {
        init();

        try (final WriteOptions writeOpt = new WriteOptions()) {
            Map<String, Integer> words;
            while ((words = queues.wordsToPersist.poll()) != null || this.options.dataStillOnStreaming()) {
                if (words != null) {
                        Long insertStart = System.currentTimeMillis();
                        System.out.printf("Persisting words into %s\n", NAME);
                        try (final WriteBatch batch = new WriteBatch()) {
                            for (Map.Entry<String, Integer> word : words.entrySet())
                                batch.merge(word.getKey().getBytes(), ByteUtils.longToBytes(word.getValue()));
                            db.write(writeOpt, batch);
                        }
                        catch (RocksDBException e) {
                            throw new RuntimeException(e);
                        }

                        Long insertEnd = System.currentTimeMillis();
                        System.out.printf("Partial Write in %d\n", insertEnd - insertStart);
                }
            }
        }

        db.close();

        System.out.printf("[DATABASE] DONE %s\n",NAME);
    }

    public RocksIterator getIterator() {
        return this.db.newIterator();
    }

    public void close() {
        this.db.close();
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
