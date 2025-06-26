package application.consumers.persistence;

import application.communication.CommunicationChanel;
import application.communication.Queues;
import application.communication.RunningOptions;

import java.util.HashMap;
import java.util.Map;

public class WordsDataBaseConsumer implements Runnable {
    final Queues queues;
    final RunningOptions options;
    final CommunicationChanel chanel;

    public WordsDataBaseConsumer(RunningOptions options, CommunicationChanel chanel, Queues queues) {
        this.queues = queues;
        this.options = options;
        this.chanel = chanel;
    }

    @Override
    public void run() {
        String word;
        Map<String, Integer> words = new HashMap<>();

        while ((word = queues.words.poll()) != null ||
                options.dataStillOnStreaming() || (this.chanel.wordsReceived.get() < chanel.wordsExpected.get())) {
            if (word != null) {
                String[] wordAndFrequency = word.split("\\s+");
                words.put(wordAndFrequency[0],
                        words.getOrDefault(wordAndFrequency[0], 0) +
                        Integer.parseInt(wordAndFrequency[1]));
                this.chanel.wordsReceived.incrementAndGet();

                if (words.size() >= 100000) {
                    Map<String, Integer> snapshot = words;
                    queues.wordsToPersist.add(snapshot);
                    words = new HashMap<>();
                }
            }
        }

        if (!words.isEmpty()) {
            queues.wordsToPersist.add(words);
        }

        this.options.onFinished();
    }
}
