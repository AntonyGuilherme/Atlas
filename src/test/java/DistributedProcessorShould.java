import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class DistributedProcessorShould {
    Map<Integer, DistributedProcessor> processors;
    LocalEmitter emitter;

    @Before
    public void setUp() {
        processors = new HashMap<>();
        emitter = new LocalEmitter(processors);
    }

    @Test
    public void createAMapFromTheDateReceived() throws InterruptedException {
        for(int i = 0; i< 3; i++)
            processors.put(i, new DistributedProcessor(emitter));

        processors.get(0).stream("file1.stream", "file4.stream");
        processors.get(1).stream("file2.stream");
        processors.get(2).stream("file3.stream");

        Thread.sleep(1000);

        Assert.assertEquals(1, processors.get(2).counter.get("house").get());
        Assert.assertEquals(5, processors.get(0).counter.get("cat").get());
        Assert.assertEquals(3, processors.get(1).counter.get("car").get());
        Assert.assertEquals(3, processors.get(0).counter.get("sun").get());
    }

    @Test
    public void remapTheContentByFrequency() throws InterruptedException {
        for(int i = 0; i< 3; i++)
            processors.put(i, new DistributedProcessor(emitter));

        processors.get(0).stream("file1.stream", "file4.stream");
        processors.get(1).stream("file2.stream");
        processors.get(2).stream("file3.stream");

        Thread.sleep(1000);

        Assert.assertEquals(1, processors.get(2).counter.get("house").get());
        Assert.assertEquals(5, processors.get(0).counter.get("cat").get());
        Assert.assertEquals(3, processors.get(1).counter.get("car").get());
        Assert.assertEquals(3, processors.get(0).counter.get("sun").get());
    }

    @Test
    public void handles() throws InterruptedException {
        DistributedProcessor processor = new DistributedProcessor(emitter);
        new Thread(() -> {
            for(int i = 0; i< 1000; i++)
                processor.handle("vasco");
        }).start();


        new Thread(() -> {
            for(int i = 0; i< 1000; i++)
                processor.handle("vasco");
        }).start();

        new Thread(() -> {
            for(int i = 0; i< 1000; i++)
                processor.handle("cruzeiro");
        }).start();

        new Thread(() -> {
            for(int i = 0; i< 1000; i++)
                processor.handle("cruzeiro");
        }).start();


        Thread.sleep(2000);

        System.out.println(processor.counter.get("cruzeiro"));
    }

}
