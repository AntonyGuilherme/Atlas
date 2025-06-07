package application;

import application.communication.CommunicationChanel;
import application.communication.Queues;
import application.consumers.persistence.WordRockRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.rocksdb.RocksIterator;

import java.io.File;
import java.nio.file.Files;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AgentTest {

    @Before
    public void setUp() throws Exception {
        File file = new File("db");
        deleteFolder(file);
    }

    public void deleteFolder(File folder) {
        File[] contents = folder.listFiles();
        if (contents != null)
            for (File f : contents)
                if (!Files.isSymbolicLink(f.toPath()))
                    deleteFolder(f);

        folder.delete();
    }

    @Test
    public void shouldShuffle() throws InterruptedException {
        Queues queues = new Queues();
        CommunicationChanel chanel = new CommunicationChanel();

        Agent agent = new Agent(0, chanel, queues);

        agent.prepareConsumers();
        agent.mapAndShuffleFiles("file1.stream", "file2.stream");

        Thread.sleep(1000);

        Map<String, Long> words = new HashMap<>();
        WordRockRepository repository = new WordRockRepository();
        repository.init();

        try (RocksIterator iterator = repository.getIterator()) {
            for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                long freq = WordRockRepository.ByteUtils.bytesToLong(iterator.value());
                String word = new String(iterator.key());
                words.put(word, freq);
            }
        }

        repository.close();

        Assert.assertEquals(3, words.size());
        Assert.assertEquals(2, words.get("sun").intValue());
        Assert.assertEquals(2, words.get("cat").intValue());
        Assert.assertEquals(2, words.get("sun").intValue());
    }
}
