package application.producers.file;

import application.communication.CommunicationChanel;
import application.communication.Queues;
import configuration.Parameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FakeLineProducer implements Runnable {
    final CommunicationChanel chanel;
    final Queues queues;

    public FakeLineProducer(CommunicationChanel chanel, Queues queues) {
        this.chanel = chanel;
        chanel.QUANTITY_OF_FILES_TO_CONSUME.incrementAndGet();
        this.queues = queues;
    }

    @Override
    public void run() {
        List<String> words = new LinkedList<>();
        words.add("car");
        words.add("bicycle");
        words.add("cruzeiro");
        words.add("atlas");
        words.add("flamengo");
        words.add("france");

        for (int i = 0; i < Parameters.NUMBER_OF_WORDS; i++) {
            Collections.shuffle(words);
            String line = words.get(0) + i + " " + words.get(1)  +i;
            queues.lines.add(line);
        }

        chanel.QUANTITY_OF_FILES_TO_CONSUME.decrementAndGet();
    }
}
