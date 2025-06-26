package application.producers.network;

import application.communication.Queues;
import application.communication.RunningOptions;
import configuration.Connection;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class WordByAgentProducer implements Runnable {
    final String host;
    final Integer port;
    final ConcurrentLinkedQueue<String> words;
    final int ownerId;
    final RunningOptions options;
    final AtomicInteger wordsTransmitted;
    final int agentId;

    public WordByAgentProducer(Integer ownerId,
                               Connection connection,
                               Integer agentId,
                               Queues queues,
                               RunningOptions options) {
        this.host = connection.HOST;
        this.port = connection.WORD_PORT;
        this.words = queues.wordsByAgent.get(agentId);
        queues.wordsTransmitted.putIfAbsent(agentId, new AtomicInteger(0));
        this.wordsTransmitted = queues.wordsTransmitted.get(agentId);
        this.ownerId = ownerId;
        this.options = options;
        this.agentId = agentId;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(host, port);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            String word;
            Map<String,Integer> frequencyByWord = new HashMap<>();
            while ((word = this.words.poll()) != null || this.options.dataStillOnStreaming()) {
                if (word != null) {
                    frequencyByWord.put(word, frequencyByWord.getOrDefault(word, 0) + 1);

                    if (frequencyByWord.size() > 10000) {
                        for (Map.Entry<String,Integer> entry : frequencyByWord.entrySet()){
                            out.write(String.format("%s %d", entry.getKey(), entry.getValue()));
                            out.newLine();
                        }

                        this.wordsTransmitted.addAndGet(frequencyByWord.size());

                        out.flush();

                        frequencyByWord.clear();
                    }
                }
            }

            if (!frequencyByWord.isEmpty()) {
                for (Map.Entry<String,Integer> entry : frequencyByWord.entrySet()){
                    out.write(String.format("%s %d", entry.getKey(), entry.getValue()));
                    out.newLine();
                }
                out.flush();
                this.wordsTransmitted.addAndGet(frequencyByWord.size());
                System.out.println("Quantity "+ wordsTransmitted.get());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.options.onFinished();
        }
    }
}
