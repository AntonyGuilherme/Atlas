package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LineProducer implements Runnable {
    final String path;
    final AgentCommunicationChanel chanel;
    final Queues queues;

    public LineProducer(String path, AgentCommunicationChanel chanel, Queues queues) {
        this.path = path;
        this.chanel = chanel;
        chanel.files.incrementAndGet();
        this.queues = queues;
    }

    @Override
    public void run() {
        File file = new File(path);

        try (BufferedReader buffer = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = buffer.readLine()) != null) {
                queues.lines.add(line);
                System.out.println(line);
            }

            chanel.files.decrementAndGet();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
