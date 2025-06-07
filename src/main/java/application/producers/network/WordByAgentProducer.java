package application.producers.network;

import application.communication.CommunicationChanel;
import application.communication.Queues;
import configuration.Parameters;
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
    final int ownerId;;
    final CommunicationChanel chanel;

    public WordByAgentProducer(Integer ownerId,
                               Connection connection,
                               Integer agentId,
                               CommunicationChanel chanel,
                               Queues queues) {
        this.host = connection.HOST;
        this.port = connection.WORD_PORT;
        this.words = queues.wordsByAgent.get(agentId);
        this.ownerId = ownerId;
        this.chanel = chanel;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(host, port);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            int words = 0;

            String word;
            Map<String,Integer> frequencyByWord = new HashMap<>();
            while ((word = this.words.poll()) != null || chanel.lines.get() > 0) {
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

                out.flush();
            }

            if (!this.chanel.finished.getAndSet(true))
                MessageProducer.flood(Parameters.FINISHED, ownerId);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
