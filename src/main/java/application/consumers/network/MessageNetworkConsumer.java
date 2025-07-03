package application.consumers.network;

import application.communication.CommunicationChanel;
import application.communication.Queues;
import application.producers.network.RangesProducer;
import configuration.Parameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageNetworkConsumer implements Runnable {
    final int port;
    final CommunicationChanel chanel;
    final Queues queues;
    final Integer agentId;

    public MessageNetworkConsumer(int port, CommunicationChanel chanel, Queues queues, Integer agentId) {
        this.port = port;
        this.chanel = chanel;
        this.queues = queues;
        this.agentId = agentId;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(200);

            while (!chanel.FINISHED.get()) {
                try(Socket socket = serverSocket.accept()){

                    try(BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    String message = buffer.readLine();

                    if (message != null) {
                        if (message.equals(Parameters.FIRST_SHUFFLE_FINISHED)) {
                            int number = Integer.parseInt(buffer.readLine());
                            chanel.wordsExpectedPartial.addAndGet(number);

                            if (chanel.agentsFinished.incrementAndGet() >= Parameters.NUMBER_OF_AGENTS) {
                                chanel.FIRST_SHUFFLE_FINISHED.set(true);
                                chanel.wordsExpected.set(chanel.wordsExpectedPartial.get());
                            }
                        } else if (message.equals(Parameters.FINISHED)) {
                            int number = Integer.parseInt(buffer.readLine());
                            chanel.wordsExpectedPartial.addAndGet(number);
                            if (chanel.allFinished.incrementAndGet() >= Parameters.NUMBER_OF_AGENTS)
                                chanel.wordsExpected.set(chanel.wordsExpectedPartial.get());
                        } else {
                            this.queues.ranges.add(message);

                            if (chanel.agentsWithMinAndMax.incrementAndGet() >= Parameters.NUMBER_OF_AGENTS) {
                                // if a range start first than everyone, I'll send words to no one
                                new Thread(new RangesProducer(queues, chanel, this.agentId)).start();
                            }
                        }
                    }
                }
                } catch (IOException e) {
                    // network timeout
                }
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
