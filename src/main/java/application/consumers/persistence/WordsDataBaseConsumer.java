package application.consumers.persistence;

import application.communication.Queues;
import application.communication.RunningOptions;

import java.util.HashMap;
import java.util.Map;

public class WordsDataBaseConsumer implements Runnable {
    final Queues queues;
    final RunningOptions options;

    public WordsDataBaseConsumer(RunningOptions options, Queues queues) {
        this.queues = queues;
        this.options = options;
    }

    @Override
    public void run() {
        String word;
        Map<String, Integer> words = new HashMap<>();

        while ((word = queues.words.poll()) != null || options.dataStillOnStreaming()) {
            if (word != null) {
                String[] wordAndFrequency = word.split("\\s+");
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

        this.options.onFinished();
    }
}
