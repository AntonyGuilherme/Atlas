package application.communication;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Queues {
    public final ConcurrentLinkedQueue<String> lines = new ConcurrentLinkedQueue<>();
    public final Hashtable<Integer, ConcurrentLinkedQueue<String>> wordsByAgent = new Hashtable<>();
    public final ConcurrentLinkedQueue<String> words = new ConcurrentLinkedQueue<>();
    public final BlockingQueue<Map<String, Integer>> wordsToPersist = new LinkedBlockingQueue<>();
    public final ConcurrentLinkedQueue<String> ranges = new ConcurrentLinkedQueue<>();
    public final ConcurrentHashMap<Integer, AtomicInteger> wordsTransmitted = new ConcurrentHashMap<>();
}
