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
    public String NAME = "romulus-t";
    public File dbPath;
    public RocksDB db;

    public void deleteFolder(File folder) {
        File[] contents = folder.listFiles();
        if (contents != null)
            for (File f : contents)
                if (!Files.isSymbolicLink(f.toPath()))
                    deleteFolder(f);

        folder.delete();
    }

    @Before
    public void setUp() {
        File file = new File("db");
        deleteFolder(file);

        RocksDB.loadLibrary();
        final Options options = new Options();
        options.setCreateIfMissing(true);
        options.setMergeOperator(new UInt64AddOperator());
        dbPath = new File("db", NAME);
        try {
            Files.createDirectories(dbPath.getParentFile().toPath());
            Files.createDirectories(dbPath.getAbsoluteFile().toPath());
            db = RocksDB.open(options, dbPath.getAbsolutePath());
            System.out.println("RocksDB initialized and ready to use");
        } catch(IOException | RocksDBException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Test
    public void bulkSavePerformance() throws RocksDBException {

        Long start = System.currentTimeMillis();

        try (final WriteOptions writeOpt = new WriteOptions()) {
            for (int i = 0; i <= 9; ++i) {
                try (final WriteBatch batch = new WriteBatch()) {
                    for (int j = 0; j <= 1000; ++j) {
                        db.merge(("key" + i).getBytes(), ByteUtils.longToBytes(1));
                    }
                    db.write(writeOpt, batch);
                }
            }
        }

        try (FlushOptions flush = new FlushOptions()){
            flush.setWaitForFlush(false);
            db.flush(flush);
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
