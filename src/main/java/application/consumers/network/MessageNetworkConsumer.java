package application.consumers.network;

import application.communication.CommunicationChanel;
import application.communication.Queues;
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
            while (chanel.allFinished.intValue() < Parameters.NUMBER_OF_AGENTS) {
                Socket socket = serverSocket.accept();

                try(BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()))){
                    String message = buffer.readLine();

                    if (message != null) {
                        if (message.equals(Parameters.FIRST_SHUFFLE_FINISHED)) {
                            int number = Integer.parseInt(buffer.readLine());
                            chanel.wordsExpectedPartial.addAndGet(number);

                            if (chanel.agentsFinished.incrementAndGet() >= Parameters.NUMBER_OF_AGENTS) {
                                chanel.FIRST_SHUFFLE_FINISHED.set(true);
                                chanel.wordsExpected.set(chanel.wordsExpectedPartial.get());
                            }
                        }

                        else if (message.equals(Parameters.FINISHED))
                            chanel.allFinished.increment();

                        else {
                            this.queues.ranges.add(message);
                            if (chanel.agentsWithMinAndMax.incrementAndGet() >= Parameters.NUMBER_OF_AGENTS){
//                                new Thread(new RangesProducer(queues, chanel, this.agentId)).start();
                            }
                        }
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            System.out.println("[FINISHED]");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
