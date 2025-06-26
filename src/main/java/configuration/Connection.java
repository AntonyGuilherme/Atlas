package configuration;

public class Connection {
    public final String HOST;
    public final Integer WORD_PORT;
    public final Integer MESSAGE_PORT;
    public final Integer agentId;

    public Connection(Integer agentId, String host, Integer port) {
        this.HOST = host;
        this.WORD_PORT = port;
        this.MESSAGE_PORT = port + Parameters.NUMBER_OF_AGENTS * Parameters.NUMBER_OF_AGENTS;
        this.agentId = agentId;
    }

    @Override
    public String toString() {
        return "Connection{" +
                "HOST='" + HOST + '\'' +
                ", WORD_PORT=" + WORD_PORT +
                ", MESSAGE_PORT=" + MESSAGE_PORT +
                '}';
    }
}
