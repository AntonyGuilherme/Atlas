package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WordConsumer implements Runnable {
        final Socket socket;
        final ConcurrentLinkedQueue<String> words;

        public WordConsumer(Socket socket, Queues queues) {
            this.socket = socket;
            this.words = queues.words;
        }

        @Override
        public void run() {
            try(BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()))){
                String word;
                while ((word = buffer.readLine()) != null)
                    words.add(word);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
}
