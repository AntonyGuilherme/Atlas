package application;

import config.Config;

public class WordProducer implements Runnable {

    final AgentCommunicationChanel chanel;
    final Queues queues;

    public WordProducer(AgentCommunicationChanel chanel, Queues queues) {
        this.chanel = chanel;
        this.chanel.lines.incrementAndGet();
        this.queues = queues;
    }

    @Override
    public void run() {
        String line;

        while ((line = queues.lines.poll()) != null || chanel.files.get() > 0) {
            if (line != null && !line.isBlank()) {
                for (String word : line.split("\\s+")) {
                    if (word != null && !word.isEmpty())
                        shuffle(word);
                }
            }
        }

        this.chanel.lines.decrementAndGet();
    }

    public void shuffle(String word) {
        long unsignedHash = word.hashCode() & 0xFFFFFFFFL;
        int hash = (int) (unsignedHash % Config.NUMBER_OF_AGENTS);

        queues.wordsByAgent.get(hash).add(word);
    }
}
