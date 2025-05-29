import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedProcessor {
    private final Emitter emitter;
    public Map<String, AtomicInteger> counter = new HashMap<>();
    public final List<AtomicInteger> operations = new LinkedList<>();
    public final List<Thread> executions = new LinkedList<>();
    public final AtomicInteger filesFinished = new AtomicInteger(0);
    private Integer amountOfFiles = 0;

    public DistributedProcessor(Emitter emitter) {
        this.emitter = emitter;
    }

    public void stream(String... files) {
        amountOfFiles = files.length;

        executions.add(new Thread(this::map));

        for (String file : files) {
            new Thread(() -> {
                operations.add(new AtomicInteger(1));
                map(file);
            }).start();
        }
    }

    public DistributedProcessor map(String filename) {
        File file = new File(filename);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                for (String word : line.split("\\s+")) {
                    if (word != null && !word.isEmpty())
                        emitter.emit(word);
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return this;
    }

    public void handle(String word) {
        if (isAnUid(word) && operations.getLast().incrementAndGet() == emitter.size()) {
            next();
            return;
        }

        counter.putIfAbsent(word, new AtomicInteger(0));
        counter.get(word).incrementAndGet();
    }

    public void finish() {
        if (filesFinished.incrementAndGet() == amountOfFiles)
            emitter.emit(UUID.randomUUID().toString());
    }

    public boolean isAnUid(String word) {
        try{
            UUID.fromString(word);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void next() {

    }

    public void map() {
        //counter.values().
    }
}
