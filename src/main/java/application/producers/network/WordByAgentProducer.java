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

public class WordByAgentProducer implements Runnable {
    final String host;
    final Integer port;
    final ConcurrentLinkedQueue<String> words;
    final int ownerId;
    final RunningOptions options;

    public WordByAgentProducer(Integer ownerId,
                               Connection connection,
                               Integer agentId,
                               Queues queues,
                               RunningOptions options) {
        this.host = connection.HOST;
        this.port = connection.WORD_PORT;
        this.words = queues.wordsByAgent.get(agentId);
        this.ownerId = ownerId;
        this.options = options;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(host, port);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            String word;
            Map<String,Integer> frequencyByWord = new HashMap<>();
            while ((word = this.words.poll()) != null || this.options.dataStillOnStreaming()) {
                if (word != null) {
                    frequencyByWord.putIfAbsent(word, 0);
                    frequencyByWord.merge(word,  1, Integer::sum);

                    if (frequencyByWord.size() > 10000) {
                        for (Map.Entry<String,Integer> entry : frequencyByWord.entrySet()){
                            out.write(String.format("%s %d", entry.getKey(), entry.getValue()));
                            out.newLine();
                        }

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

                System.out.println(frequencyByWord.size());

                out.flush();
            }

            this.options.onFinished();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
