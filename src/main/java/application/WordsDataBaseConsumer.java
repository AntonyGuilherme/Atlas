package application;

import java.util.HashMap;
import java.util.Map;

public class WordsDataBaseConsumer implements Runnable {
    final Queues queues;
    final AgentCommunicationChanel chanel;

    public WordsDataBaseConsumer(AgentCommunicationChanel chanel, Queues queues) {
        this.queues = queues;
        this.chanel = chanel;
    }

    @Override
    public void run() {
        String word;
        Map<String, Integer> words = new HashMap<>();

        while ((word = queues.words.poll()) != null || !chanel.shuffling.get()) {
            if (word != null) {
                words.put(word, words.getOrDefault(word, 0) + 1);

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
