package application.communication;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class CommunicationChanel {
    public final AtomicInteger QUANTITY_OF_FILES_TO_CONSUME = new AtomicInteger(0);
    public final AtomicInteger LINES_PRODUCER_RUNNING = new AtomicInteger(0);
    public final AtomicBoolean FIRST_SHUFFLE_FINISHED = new AtomicBoolean(false);

    public final AtomicInteger agentsFinished = new AtomicInteger(0);
    public final AtomicBoolean persistenceFinished = new AtomicBoolean(false);
    public final AtomicInteger agentsWithMinAndMax = new AtomicInteger(0);
    public final LongAdder allFinished = new LongAdder();
    public final AtomicInteger wordsExpectedPartial = new AtomicInteger(0);
    public final AtomicInteger wordsExpected = new AtomicInteger(Integer.MAX_VALUE);
    public final AtomicInteger wordsReceived = new AtomicInteger(0);
}
