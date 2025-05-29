package application;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AgentCommunicationChanel {
    public AtomicBoolean finished = new AtomicBoolean(false);
    public final AtomicInteger files = new AtomicInteger(0);
    public final AtomicInteger lines = new AtomicInteger(0);
    public final AtomicBoolean shuffling = new AtomicBoolean(false);
    public final AtomicInteger agentsFinished = new AtomicInteger(0);
}
