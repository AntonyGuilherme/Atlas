package application.communication;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class CommunicationChanel {
    public AtomicBoolean FILE_WORDS_PRODUCING_IS_FINISHED = new AtomicBoolean(false);
    public final AtomicInteger QUANTITY_OF_FILES_TO_CONSUME = new AtomicInteger(0);
    public final AtomicInteger LINES_PRODUCER_RUNNING = new AtomicInteger(0);
    public final AtomicBoolean FIRST_SHUFFLE_FINISHED = new AtomicBoolean(false);

    public final AtomicInteger agentsFinished = new AtomicInteger(0);
    public final AtomicBoolean persistenceFinished = new AtomicBoolean(false);
    public final AtomicInteger agentsWithMinAndMax = new AtomicInteger(0);
    public final LongAdder allFinished = new LongAdder();
}
