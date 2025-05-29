package application;

import config.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageNetworkConsumer implements Runnable {
    final int port;
    final AgentCommunicationChanel chanel;

    public MessageNetworkConsumer(int port, AgentCommunicationChanel chanel) {
        this.port = port;
        this.chanel = chanel;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!chanel.shuffling.get()) {
                Socket socket = serverSocket.accept();

                try(BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()))){
                    String message = buffer.readLine();

                    while (message != null) {
                        chanel.shuffling.set(
                                message.equals(Config.FINISHED) &&
                                chanel.agentsFinished.incrementAndGet() == Config.NUMBER_OF_AGENTS);

                        message = buffer.readLine();
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
