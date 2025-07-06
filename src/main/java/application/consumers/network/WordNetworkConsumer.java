package application.consumers.network;

import application.communication.CommunicationChanel;
import application.communication.Queues;
import application.consumers.persistence.WordConsumer;
import configuration.Parameters;

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
        Long start = System.currentTimeMillis();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(200);
            while (!chanel.FINISHED.get()) {
                try{
                    Socket socket = serverSocket.accept();
                    new Thread(new WordConsumer(socket, queues)).start();
                }
                catch (IOException e){}
            }

            Long end = System.currentTimeMillis();
            System.out.println("ran in " + (end - start));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
