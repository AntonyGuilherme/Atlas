package application;

import application.communication.CommunicationChanel;
import application.communication.Queues;
import application.consumers.persistence.ReShuffleRockPersistence;
import application.consumers.persistence.WordRockRepository;
import application.producers.network.RangesProducer;
import com.google.common.collect.RangeMap;
import configuration.Parameters;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.rocksdb.RocksIterator;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
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
        ReShuffleRockPersistence repository = new ReShuffleRockPersistence();
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

    @Test
    public void shouldCreateTheRanges() {
        Queue<String> ranges = new LinkedList<>();
        ranges.add("1,2");
        ranges.add("3,4");
        ranges.add("5,6");
        ranges.add("7,8");

        RangeMap<Integer, Integer> agentByRanges = RangesProducer.RangeCreator.getRangeMap(ranges, 1);

        for (int i = 1; i <= 8; i++)
            Assert.assertEquals(0, agentByRanges.get(i).intValue());
    }

    @Test
    public void shouldCreateTheRangesWithSameNumberOfAgentsAndRanges() {
        Queue<String> ranges = new LinkedList<>();
        ranges.add("1,2");
        ranges.add("3,4");
        ranges.add("5,6");
        ranges.add("7,8");

        RangeMap<Integer, Integer> agentByRanges = RangesProducer.RangeCreator.getRangeMap(ranges, 8);

        for (int i = 1; i <= 8; i++){
            Assert.assertNotNull(agentByRanges.get(i));
            Assert.assertEquals(8 - i, agentByRanges.get(i).intValue());
        }
    }

    @Test
    public void shouldCreateTheRangesWithDifferentNumberOfAgentsAndRanges() {
        Queue<String> ranges = new LinkedList<>();
        ranges.add("1,2");
        ranges.add("3,4");

        RangeMap<Integer, Integer> agentByRanges = RangesProducer.RangeCreator.getRangeMap(ranges, 2);

        Assert.assertEquals(0, agentByRanges.get(4).intValue());
        Assert.assertEquals(0, agentByRanges.get(3).intValue());
        Assert.assertEquals(1, agentByRanges.get(2).intValue());
        Assert.assertEquals(1, agentByRanges.get(1).intValue());
    }

    @Test
    public void shouldCreateTheRangesWithLessAgentsThanRanges() {
        Queue<String> ranges = new LinkedList<>();
        ranges.add("1,2");
        ranges.add("3,5");

        RangeMap<Integer, Integer> agentByRanges = RangesProducer.RangeCreator.getRangeMap(ranges, 2);

        Assert.assertEquals(0, agentByRanges.get(5).intValue());
        Assert.assertEquals(0, agentByRanges.get(4).intValue());
        Assert.assertEquals(0, agentByRanges.get(3).intValue());
        Assert.assertEquals(1, agentByRanges.get(2).intValue());
        Assert.assertEquals(1, agentByRanges.get(1).intValue());
    }

    @Test
    public void shouldCreateTheRangesWithLessAgentsThanRangesConsideringHighRanges() {
        Queue<String> ranges = new LinkedList<>();
        ranges.add("1,2");
        ranges.add("3,8");

        RangeMap<Integer, Integer> agentByRanges = RangesProducer.RangeCreator.getRangeMap(ranges, 3);

        Assert.assertEquals(0, agentByRanges.get(8).intValue());
        Assert.assertEquals(0, agentByRanges.get(7).intValue());
        Assert.assertEquals(0, agentByRanges.get(6).intValue());
        Assert.assertEquals(1, agentByRanges.get(5).intValue());
        Assert.assertEquals(1, agentByRanges.get(4).intValue());
        Assert.assertEquals(1, agentByRanges.get(3).intValue());
        Assert.assertEquals(2, agentByRanges.get(2).intValue());
        Assert.assertEquals(2, agentByRanges.get(1).intValue());
    }
}
