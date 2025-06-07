package application.consumers.persistence;

import application.communication.CommunicationChanel;
import application.communication.Queues;

import java.util.HashMap;
import java.util.Map;

public class WordsDataBaseConsumer implements Runnable {
    final Queues queues;
    final CommunicationChanel chanel;

    public WordsDataBaseConsumer(CommunicationChanel chanel, Queues queues) {
        this.queues = queues;
        this.chanel = chanel;
    }

    @Override
    public void run() {
        String word;
        Map<String, Integer> words = new HashMap<>();

        while ((word = queues.words.poll()) != null || !chanel.shuffling.get()) {
            if (word != null) {
                String[] wordAndFrequency = word.split(" ");
                words.putIfAbsent(wordAndFrequency[0], 0);
                words.merge(wordAndFrequency[0], Integer.parseInt(wordAndFrequency[1]), Integer::sum);

                if (words.size() >= 100000) {
                    Map<String, Integer> snapshot = words;
                    queues.wordsToPersist.add(snapshot);
                    words = new HashMap<>();
                }
            }
        }

        if (!words.isEmpty()) {
            System.out.println(words.size() + " words");
            queues.wordsToPersist.add(words);
        }

        chanel.persistenceFinished.set(true);
    }
}
