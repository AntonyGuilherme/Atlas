package config;

public class Connection {
    public final String HOST;
    public final Integer WORD_PORT;
    public final Integer MESSAGE_PORT;

    public Connection(String host, Integer port) {
        this.HOST = host;
        this.WORD_PORT = port;
        this.MESSAGE_PORT = port + Config.NUMBER_OF_AGENTS * Config.NUMBER_OF_AGENTS;
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
