package application;

import contracts.Emitter;
import org.junit.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkEmitterTests {

    @Test
    public void shouldBePossibleToSendAMessage() throws InterruptedException {
        final String host = "localhost";
        final int port = 8080;

        Runnable handler = new NetworkHandler(host, port);
        Runnable emitter = new NetworkEmitter(host, port);
        new Thread(handler).start();
        new Thread(emitter).start();
        new Thread(emitter).start();

        Thread.sleep(100);
    }

    class NetworkHandler implements Runnable {
        final String host;
        final Integer port;

        public NetworkHandler(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Server started on port " + port);
                int clients = 2;
                while (clients > 0) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Client " + clients + " connected");
                    clients--;
                    new Thread(new WordsProcessor(socket)).start();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class WordsProcessor implements Runnable {
        final Socket socket;

        public WordsProcessor(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try(BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()))){
                String line;
                while ((line = buffer.readLine()) != null){
                    System.out.println(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class NetworkEmitter implements Runnable {
        final String host;
        final Integer port;

        public NetworkEmitter(String host, Integer port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            try (Socket socket = new Socket(host, port);
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

                for (int i = 0; i < 100; i++) {
                    out.write("atlas");
                    out.newLine();
                }

                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
