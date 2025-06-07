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

    public MessageNetworkConsumer(int port, CommunicationChanel chanel, Queues queues) {
        this.port = port;
        this.chanel = chanel;
        this.queues = queues;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!chanel.allFinished.get()) {
                Socket socket = serverSocket.accept();

                try(BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()))){
                    String message = buffer.readLine();

                    while (message != null) {
                        if (message.equals(Parameters.FINISHED))
                            chanel.shuffling.set(chanel.agentsFinished.incrementAndGet() >= Parameters.NUMBER_OF_AGENTS);
                        else {
                            chanel.allFinished.set(chanel.agentsWithMinAndMax.incrementAndGet() >= Parameters.NUMBER_OF_AGENTS);
                            this.queues.ranges.add(message);
                        }

                        message = buffer.readLine();
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
