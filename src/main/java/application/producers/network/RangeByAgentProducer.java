package application.producers.network;

import application.communication.Queues;
import application.communication.RunningOptions;
import configuration.Connection;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class RangeByAgentProducer implements Runnable {
    final String host;
    final Integer port;
    final ConcurrentLinkedQueue<String> words;
    final int ownerId;
    final RunningOptions options;
    final AtomicInteger wordsTransmitted;
    final int agentId;

    public RangeByAgentProducer(Integer ownerId,
                               Connection connection,
                               Integer agentId,
                               Queues queues,
                               RunningOptions options) {
        this.host = connection.HOST;
        this.port = connection.WORD_PORT;
        this.words = queues.wordsByAgent.get(agentId);
        this.wordsTransmitted = queues.wordsTransmitted.get(agentId);
        this.ownerId = ownerId;
        this.options = options;
        this.agentId = agentId;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(host, port);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            String wordAndFrequency;
            int linesPrepared = 0;
            while ((wordAndFrequency = this.words.poll()) != null || this.options.dataStillOnStreaming()) {
                if (wordAndFrequency != null) {

                    out.write(wordAndFrequency);
                    out.newLine();

                    linesPrepared++;
                    if (linesPrepared > 10000) {
                        wordsTransmitted.addAndGet(linesPrepared);
                        out.flush();
                        linesPrepared = 0;
                    }
                }
            }


            if (linesPrepared > 0) {
                out.flush();
                wordsTransmitted.addAndGet(linesPrepared);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.options.onFinished();
        }
    }
}
