package application;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class AgentTest {

    @Test
    public void shouldShuffle() throws InterruptedException {
        Agent agent = new Agent(0);
//        Agent agent1 = new Agent(1);
//        Agent agent2 = new Agent(2);

        agent.prepareConsumers();
//        agent1.prepareConsumers();
//        agent2.prepareConsumers();

        //agent.mapAndShuffleFiles("CC-MAIN-20230321002050-20230321032050-00446.warc.wet");
        agent.mapAndShuffleFiles("file1.stream", "file2.stream");
//        agent2.mapAndShuffleFiles("file1.stream", "file2.stream");

        Thread.sleep(2000);
    }

    @Test
    public void test() {
        System.out.println((int)2.5);
    }

    public static void tt() {
        AtomicInteger teste =  new AtomicInteger(0);
        for (int i = 0; i < 100; i++) {
            teste.incrementAndGet();
        }

        System.out.println(teste.get());
    }
}
