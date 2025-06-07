package application.consumers.network;

import application.communication.CommunicationChanel;
import application.communication.Queues;
import application.consumers.persistence.WordConsumer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WordNetworkConsumer implements Runnable {
    final int port;
    final CommunicationChanel chanel;
    final Queues queues;

    public WordNetworkConsumer(int port, CommunicationChanel chanel, Queues queues) {
        this.port = port;
        this.chanel = chanel;
        this.queues = queues;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (!chanel.shuffling.get()) {
                Socket socket = serverSocket.accept();
                new Thread(new WordConsumer(socket, queues)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
