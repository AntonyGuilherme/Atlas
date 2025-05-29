package application;

import config.Config;
import config.Connection;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WordNetworkEmitter implements Runnable {
    final String host;
    final Integer port;
    final ConcurrentLinkedQueue<String> words;
    final int ownerId;;
    final AgentCommunicationChanel chanel;


    public WordNetworkEmitter(Integer ownerId,
                              Connection connection,
                              Integer agentId,
                              AgentCommunicationChanel chanel,
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
            while ((word = this.words.poll()) != null || chanel.lines.get() > 0) {
                if (word != null) {
                    out.write(word);
                    out.newLine();
                    words++;

                    if (words >= 1000){
                        out.flush();
                        words = 0;
                    }
                }
            }

            if (words > 0)
                out.flush();

            if (!this.chanel.finished.getAndSet(true)) {
                System.out.println("[NETWORK] Emission is Finished");
                NetworkMessageEmitter.flood(Config.FINISHED, ownerId);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
