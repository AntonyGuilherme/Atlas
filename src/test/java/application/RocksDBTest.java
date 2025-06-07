package application;

import org.junit.Before;
import org.junit.Test;
import org.rocksdb.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class RocksDBTest {
    public String NAME = "romulus";
    public File dbPath;
    public RocksDB db;

    @Before
    public void setUp() {
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

    public void save(String key) {
        try {
            db.merge(key.getBytes(), ByteUtils.longToBytes(1));
        } catch (RocksDBException e) {
           System.out.println(e.getMessage());
        }
    }

    @Test
    public void bulkSavePerformance() throws RocksDBException {

        Long start = System.currentTimeMillis();

        try (final WriteOptions writeOpt = new WriteOptions()) {
            for (int i = 0; i <= 9; ++i) {
                try (final WriteBatch batch = new WriteBatch()) {
                    for (int j = 0; j <= 100000; ++j) {
                        batch.merge(("key" + i).getBytes(), ByteUtils.longToBytes(1));
                    }
                    db.write(writeOpt, batch);
                }
            }
        }
        Long end = System.currentTimeMillis();

        System.out.println("Time taken: " + (end - start) + "ms");

        try (RocksIterator iterator = db.newIterator()) {
            for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                String word = new String(iterator.key(), StandardCharsets.UTF_8);
                long freq = ByteUtils.bytesToLong(iterator.value());

                System.out.println(word + " = " + freq);
            }
        }
    }

    static class ByteUtils {
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
