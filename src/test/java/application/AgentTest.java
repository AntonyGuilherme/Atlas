package application;

import org.junit.Test;

public class AgentTest {
    @Test
    public void shouldShuffle() throws InterruptedException {
        Agent agent = new Agent(0);
        Agent agent1 = new Agent(1);
        Agent agent2 = new Agent(2);

        agent.prepareConsumers();
        agent1.prepareConsumers();
        agent2.prepareConsumers();

        agent.mapAndShuffleFiles("file1.stream", "file4.stream");
        agent1.mapAndShuffleFiles("file2.stream");
        agent2.mapAndShuffleFiles("file3.stream");

        Thread.sleep(100);
    }
}
