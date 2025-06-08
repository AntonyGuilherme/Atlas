package application.producers.file;

import application.communication.CommunicationChanel;
import application.communication.Queues;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LineProducer implements Runnable {
    final String path;
    final CommunicationChanel chanel;
    final Queues queues;

    public LineProducer(String path, CommunicationChanel chanel, Queues queues) {
        this.path = path;
        this.chanel = chanel;
        chanel.QUANTITY_OF_FILES_TO_CONSUME.incrementAndGet();
        this.queues = queues;
    }

    @Override
    public void run() {
        File file = new File(path);

        try (BufferedReader buffer = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = buffer.readLine()) != null)
                queues.lines.add(line);

            chanel.QUANTITY_OF_FILES_TO_CONSUME.decrementAndGet();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
