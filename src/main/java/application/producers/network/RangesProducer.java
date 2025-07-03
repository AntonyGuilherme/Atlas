package application.producers.network;

import application.Agent;
import application.communication.CommunicationChanel;
import application.communication.Queues;
import application.communication.RunningOptions;
import application.consumers.persistence.ReShuffleRockPersistence;
import application.consumers.persistence.WordsDataBaseConsumer;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import configuration.Connection;
import configuration.Parameters;
import application.consumers.persistence.WordRockRepository;
import org.rocksdb.RocksIterator;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RangesProducer implements Runnable {
    final Queues queues;
    final Integer agentId;
    final CommunicationChanel chanel;

    public RangesProducer(Queues queues, CommunicationChanel chanel, Integer agentId) {
        this.queues = queues;
        this.agentId = agentId;
        this.chanel = chanel;
    }

    @Override
    public void run() {
        RangeMap<Integer, Integer> ranges = RangeCreator.getRangeMap(queues.ranges, Parameters.NUMBER_OF_AGENTS);

        chanel.wordsExpected.set(Integer.MAX_VALUE);
        chanel.wordsExpectedPartial.set(0);
        chanel.wordsReceived.set(0);
        new Thread(new WordsDataBaseConsumer(new ReShuffleRunningOptions(chanel), chanel, queues)).start();
        new Thread(new ReShuffleRockPersistence(queues, new ReShufflePersistenceOptions(chanel), agentId)).start();

        AtomicBoolean running = new AtomicBoolean(true);
        RangeOptions options = new RangeOptions(running, this.agentId);

        List<Connection> connections = Parameters.EMIT_TO.get(agentId);
        for (int otherId = 0; otherId < Parameters.NUMBER_OF_AGENTS; otherId++) {
            queues.wordsTransmitted.get(otherId).set(0);
            new Thread(new RangeByAgentProducer(agentId,
                    connections.get(otherId),
                    otherId, queues, options)).start();
        }
        WordRockRepository repository = new WordRockRepository(this.agentId);
        repository.init();

        try (RocksIterator iterator = repository.getIterator()) {
            for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                long freq = WordRockRepository.ByteUtils.bytesToLong(iterator.value());
                Integer agentId = ranges.get((int) freq);
                String word = new String(iterator.key());
                queues.wordsByAgent.get(agentId).add(String.format("%s %d",word,freq));
            }
        }

        repository.close();

        // for some reason the queue is not empty when the consumers are being finished
        for (int otherId = 0; otherId < Parameters.NUMBER_OF_AGENTS; otherId++)
            while (!queues.wordsByAgent.get(agentId).isEmpty());

        running.set(false);
    }

    public class RangeOptions implements RunningOptions {
        private final Integer agentId;
        private final AtomicBoolean finished;
        private final AtomicInteger running = new AtomicInteger(0);

        public RangeOptions(AtomicBoolean finished, Integer agentId) {
            this.finished = finished;
            this.agentId = agentId;
        }

        @Override
        public boolean dataStillOnStreaming() {
            return this.finished.get();
        }

        @Override
        public void onFinished() {
            if (running.incrementAndGet() >= Parameters.NUMBER_OF_AGENTS)
                MessageProducer.floodFinishFirstShuffling(queues, Parameters.FINISHED, agentId);
        }
    }

    public class ReShuffleRunningOptions implements RunningOptions {

        private final CommunicationChanel chanel;

        public ReShuffleRunningOptions(CommunicationChanel chanel) {
            this.chanel = chanel;
            this.chanel.persistenceFinished.set(false);
        }

        @Override
        public boolean dataStillOnStreaming() {
            return this.chanel.allFinished.get() < Parameters.NUMBER_OF_AGENTS;
        }

        @Override
        public void onFinished() {
            this.chanel.persistenceFinished.set(true);
        }
    }

    public class ReShufflePersistenceOptions implements RunningOptions {

        private final CommunicationChanel chanel;

        public ReShufflePersistenceOptions(CommunicationChanel chanel) {
            this.chanel = chanel;
        }

        @Override
        public boolean dataStillOnStreaming() {
            return chanel.wordsReceived.get() < chanel.wordsExpected.get();
        }

        @Override
        public void onFinished() {
            chanel.FINISHED.set(true);
        }
    }

    public static class RangeCreator {
        public static RangeMap<Integer, Integer> getRangeMap(Queue<String> ranges, int numberOfAgents) {
            int max = 0;
            int min = Integer.MAX_VALUE;
            for (String range : ranges){
                String[] minAndMax = range.split(",");
                int partialMin = Integer.parseInt(minAndMax[0]);
                int partialMax = Integer.parseInt(minAndMax[1]);

                if (partialMax > max) max = partialMax;
                if (partialMin < min) min = partialMin;
            }

            int difference = (int) Math.round(0.5 + (double) (max - min) / numberOfAgents);

            RangeMap<Integer, Integer> agentsByRanges = TreeRangeMap.create();

            for (int i = 0; i < numberOfAgents; i++) {
                int agentMin = max - difference * (i + 1);
                int agentMax = agentMin + difference;
                if (agentMin > 1) agentMin++;
                agentsByRanges.put(Range.closed(agentMin, agentMax), i);
            }

            return agentsByRanges;
        }
    }
}
