package application;

import java.util.Hashtable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Queues {
    public final ConcurrentLinkedQueue<String> lines = new ConcurrentLinkedQueue<>();
    public final Hashtable<Integer, ConcurrentLinkedQueue<String>> wordsByAgent = new Hashtable<>();
    public final ConcurrentLinkedQueue<String> words = new ConcurrentLinkedQueue<>();
}
