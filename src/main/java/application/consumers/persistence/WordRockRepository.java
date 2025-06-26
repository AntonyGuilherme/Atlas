package application.consumers.persistence;

import application.communication.RunningOptions;
import application.producers.network.MessageProducer;
import application.communication.Queues;
import org.rocksdb.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Map;

public class WordRockRepository implements Runnable {
    RunningOptions options;
    Queues queues;
    final String NAME;
    Integer ownerId;
    File dbPath;
    RocksDB db;

    public WordRockRepository(Queues queues, RunningOptions options, Integer ownerId) {
        this.queues = queues;
        this.ownerId = ownerId;
        this.options = options;
        NAME = String.format("romulus%d", ownerId);
    }

    public WordRockRepository(Integer ownerId) {
        NAME = String.format("romulus%d", ownerId);
    }

    public void init() {
        RocksDB.loadLibrary();
        final Options options = new Options();
        options.setCreateIfMissing(true);
        options.setMergeOperator(new UInt64AddOperator());
        dbPath = new File("db", NAME);
        try {
            Files.createDirectories(dbPath.getParentFile().toPath());
            Files.createDirectories(dbPath.getAbsoluteFile().toPath());
            db = RocksDB.open(options, dbPath.getAbsolutePath());
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
            while ((words = queues.wordsToPersist.poll()) != null || this.options.dataStillOnStreaming()) {
                if (words != null) {
                    System.out.printf("database %d save bulk\n", ownerId);
                        try (final WriteBatch batch = new WriteBatch()) {
                            for (Map.Entry<String, Integer> word : words.entrySet()){
                                batch.merge(word.getKey().getBytes(), ByteUtils.longToBytes(word.getValue()));
                                System.out.printf("[DATABASE] %s %d\n", word.getKey(), word.getValue());
                                System.out.println(ByteUtils.bytesToLong(ByteUtils.longToBytes(word.getValue())));
                            }

                            db.write(writeOpt, batch);
                        }
                        catch (RocksDBException e) {
                            throw new RuntimeException(e);
                        }
                }
            }
        }

        try {
            db.flushWal(true);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }

        Long end = System.currentTimeMillis();

        System.out.println("RocksDB write completed in " + (end - start) + "ms");

        long max = 1;
        long min = 1;

//        try (RocksIterator iterator = db.newIterator()) {
//            for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
//                long freq = ByteUtils.bytesToLong(iterator.value());
//                if (max < freq) max = freq;
//                else if (min > freq) min = freq;
//            }
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//
//        MessageProducer.flood(String.format("%d,%d", min,max), this.ownerId);

        db.close();
    }

    public RocksIterator getIterator() {
        return db.newIterator();
    }

    public void close() {
        db.close();
    }

    public static class ByteUtils {
        public static byte[] longToBytes(long x) {
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong(0, x);
            return buffer.array();
        }

        public static long bytesToLong(byte[] bytes) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            return buffer.getLong();
        }
    }
}
