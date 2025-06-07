package config;

import application.Agent;

import java.util.*;

public class Config {
    public static final String HOST = "localhost";
    public final static Integer NUMBER_OF_AGENTS = 1;
    public final static Map<Integer, List<Connection>> LISTEN_TO = new HashMap<>();
    public final static Map<Integer, List<Connection>> EMIT_TO = new HashMap<>();

    static {

        final String[] HOSTS = new String[NUMBER_OF_AGENTS];
        Arrays.fill(HOSTS, HOST);

        for (int i  = 0; i < NUMBER_OF_AGENTS; i++) {
            LISTEN_TO.put(i, new ArrayList<>());
            EMIT_TO.put(i, new ArrayList<>());
        }

        int adder = 0;
        for (int i  = 0; i < NUMBER_OF_AGENTS; i++) {
            for (int j = 0; j < NUMBER_OF_AGENTS; j++) {
                LISTEN_TO.get(i).add(new Connection(HOSTS[i], 400+ adder));
                EMIT_TO.get(j).add(new Connection(HOSTS[i], 400 + adder));
                adder++;
            }
        }
    }

    public final static String FINISHED = "7ca6af8b-bb9a-42a1-99ac-e5f8cf368012";
}
