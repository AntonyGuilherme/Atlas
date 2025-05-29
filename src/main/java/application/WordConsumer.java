package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class WordConsumer implements Runnable {
        final Socket socket;

        public WordConsumer(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try(BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()))){
                String word;
                while ((word = buffer.readLine()) != null)
                    System.out.println(word);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
}
