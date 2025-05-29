package application;

import config.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WordNetworkConsumer implements Runnable {
    final int port;
    final AgentCommunicationChanel chanel;

    public WordNetworkConsumer(int port, AgentCommunicationChanel chanel) {
        this.port = port;
        this.chanel = chanel;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Waiting for incoming connection...");

            while (!chanel.shuffling.get()) {
                Socket socket = serverSocket.accept();
                new Thread(new WordConsumer(socket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("WORD CONSUMER STOPS "+ port);
    }
}
