package application;

import config.Config;
import config.Connection;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Agent {

    final int agentId;
    final AgentCommunicationChanel chanel;
    final Queues queues;

    public Agent (int agentId) {
        this.agentId = agentId;
        chanel = new AgentCommunicationChanel();
        queues = new Queues();

        for (int i = 0; i < Config.NUMBER_OF_AGENTS; i++)
            queues.wordsByAgent.put(i, new ConcurrentLinkedQueue<>());
    }

    public void prepareConsumers() {
        // messages from every other agent, so one by agent is enough
        for (Connection connection: Config.LISTEN_TO.get(agentId)) {
            System.out.println("Agent " + agentId + " is connecting to " + connection);
            new Thread(new MessageNetworkConsumer(connection.MESSAGE_PORT, chanel)).start();
            new Thread(new WordNetworkConsumer(connection.WORD_PORT, chanel)).start();
        }
    }

    public void mapAndShuffleFiles(String... files) {
        for (String file : files){
            new Thread(new LineProducer(file, chanel, queues)).start();
            new Thread(new WordProducer(chanel, queues)).start();
            // one emitter by agent and file, so the total number would be files times agents
            List<Connection> connections = Config.EMIT_TO.get(agentId);
            for (int otherId = 0; otherId < Config.NUMBER_OF_AGENTS; otherId++)
                new Thread(new WordNetworkEmitter(agentId,
                        connections.get(otherId),
                        otherId, chanel, queues)).start();
        }
    }
}
