package application.producers.file;

import application.communication.CommunicationChanel;
import application.communication.Queues;
import configuration.Parameters;

public class WordProducer implements Runnable {

    final CommunicationChanel chanel;
    final Queues queues;

    public WordProducer(CommunicationChanel chanel, Queues queues) {
        this.chanel = chanel;
        this.chanel.LINES_PRODUCER_RUNNING.incrementAndGet();
        this.queues = queues;
    }

    @Override
    public void run() {
        String line;

        while ((line = queues.lines.poll()) != null || chanel.QUANTITY_OF_FILES_TO_CONSUME.get() > 0) {
            if (line != null && !line.isBlank()) {
                for (String word : line.split("\\s+")) {
                    if (word != null && !word.isEmpty())
                        shuffle(word);
                }
            }
        }

        this.chanel.LINES_PRODUCER_RUNNING.decrementAndGet();
    }

    public void shuffle(String word) {
        long unsignedHash = word.hashCode() & 0xFFFFFFFFL;
        int hash = (int) (unsignedHash % Parameters.NUMBER_OF_AGENTS);

        queues.wordsByAgent.get(hash).add(word);
    }
}
