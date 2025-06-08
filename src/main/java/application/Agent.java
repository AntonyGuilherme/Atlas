package application;

import application.communication.CommunicationChanel;
import application.communication.Queues;
import application.communication.RunningOptions;
import application.consumers.network.MessageNetworkConsumer;
import application.consumers.network.WordNetworkConsumer;
import application.consumers.persistence.WordsDataBaseConsumer;
import application.producers.file.LineProducer;
import application.producers.file.WordProducer;
import application.producers.network.MessageProducer;
import application.producers.network.WordByAgentProducer;
import configuration.Parameters;
import configuration.Connection;
import application.consumers.persistence.WordRockRepository;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Agent {
    final int agentId;
    final CommunicationChanel chanel;
    final Queues queues;

    public Agent (int agentId, CommunicationChanel chanel, Queues queues) {
        this.agentId = agentId;
        this.chanel = chanel;
        this.queues = queues;

        for (int i = 0; i < Parameters.NUMBER_OF_AGENTS; i++)
            queues.wordsByAgent.put(i, new ConcurrentLinkedQueue<>());
    }

    public void prepareConsumers() {
        // messages from every other agent, so one by agent is enough
        for (Connection connection: Parameters.LISTEN_TO.get(agentId)) {
            new Thread(new MessageNetworkConsumer(connection.MESSAGE_PORT, chanel, queues, this.agentId)).start();
            new Thread(new WordNetworkConsumer(connection.WORD_PORT, chanel, queues)).start();
        }

        new Thread(new WordsDataBaseConsumer(new WordFromFilesRunningOptions(chanel), queues)).start();
        new Thread(new WordRockRepository(queues, new SaveWordsFromFileRunningOptions(chanel), agentId)).start();
    }

    public void mapAndShuffleFiles(String... files) {
        for (String file : files){
            new Thread(new LineProducer(file, chanel, queues)).start();
            new Thread(new WordProducer(chanel, queues)).start();
            // one emitter by agent and file, so the total number would be files times agents
            List<Connection> connections = Parameters.EMIT_TO.get(agentId);
            RunningOptions options = new FileRunningOptions(chanel, agentId);
            for (int otherId = 0; otherId < Parameters.NUMBER_OF_AGENTS; otherId++)
                new Thread(new WordByAgentProducer(agentId,
                        connections.get(otherId),
                        otherId, queues, options)).start();
        }
    }

    public class FileRunningOptions implements RunningOptions {

        private final Integer agentId;
        private final CommunicationChanel chanel;

        public FileRunningOptions(CommunicationChanel chanel, Integer agentId) {
            this.chanel = chanel;
            this.agentId = agentId;
        }

        @Override
        public boolean dataStillOnStreaming() {
            return chanel.LINES_PRODUCER_RUNNING.get() > 0;
        }

        @Override
        public void onFinished() {
            if (!this.chanel.FILE_WORDS_PRODUCING_IS_FINISHED.getAndSet(true))
                MessageProducer.flood(Parameters.FIRST_SHUFFLE_FINISHED, this.agentId);
        }
    }

    public class WordFromFilesRunningOptions implements RunningOptions {

        private final CommunicationChanel chanel;

        public WordFromFilesRunningOptions(CommunicationChanel chanel) {
            this.chanel = chanel;
        }

        @Override
        public boolean dataStillOnStreaming() {
            return !chanel.FIRST_SHUFFLE_FINISHED.get();
        }

        @Override
        public void onFinished() {
            chanel.persistenceFinished.set(true);
        }
    }

    public class SaveWordsFromFileRunningOptions implements RunningOptions {

        private final CommunicationChanel chanel;
        public SaveWordsFromFileRunningOptions(CommunicationChanel chanel) {
            this.chanel = chanel;
        }

        @Override
        public boolean dataStillOnStreaming() {
            return !chanel.persistenceFinished.get();
        }

        @Override
        public void onFinished() {}
    }
}
