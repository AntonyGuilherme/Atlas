package configuration;

import java.util.*;

public class Parameters {
    public static int NUMBER_OF_WORDS = 1500000;
    public static Integer NUMBER_OF_AGENTS = 4;
    public final static Map<Integer, List<Connection>> LISTEN_TO = new HashMap<>();
    public final static Map<Integer, List<Connection>> EMIT_TO = new HashMap<>();

    static {
        init();
    }

    public static void init() {
        LISTEN_TO.clear();
        EMIT_TO.clear();

        final String[] HOSTS = new String[] {
                "tp-1a201-01.enst.fr",
                "tp-1a201-04.enst.fr",
                "tp-1a201-06.enst.fr",
                "tp-1a201-07.enst.fr"
        };

        for (int i  = 0; i < NUMBER_OF_AGENTS; i++) {
            LISTEN_TO.put(i, new ArrayList<>());
            EMIT_TO.put(i, new ArrayList<>());
        }

        int adder = 0;
        for (int i  = 0; i < NUMBER_OF_AGENTS; i++) {
            for (int j = 0; j < NUMBER_OF_AGENTS; j++) {
                LISTEN_TO.get(i).add(new Connection(j, HOSTS[i], 2000 + adder));
                EMIT_TO.get(j).add(new Connection(i, HOSTS[i], 2000 + adder));
                adder++;
            }
        }
    }

    public static void init(String[] HOSTS) {
        LISTEN_TO.clear();
        EMIT_TO.clear();

        NUMBER_OF_AGENTS = HOSTS.length;

        for (int i  = 0; i < NUMBER_OF_AGENTS; i++) {
            LISTEN_TO.put(i, new ArrayList<>());
            EMIT_TO.put(i, new ArrayList<>());
        }

        int adder = 0;
        for (int i  = 0; i < NUMBER_OF_AGENTS; i++) {
            for (int j = 0; j < NUMBER_OF_AGENTS; j++) {
                LISTEN_TO.get(i).add(new Connection(j, HOSTS[i], 2000 + adder));
                EMIT_TO.get(j).add(new Connection(i, HOSTS[i], 2000 + adder));
                adder++;
            }
        }
    }


    public final static String FIRST_SHUFFLE_FINISHED = "7ca6af8b-bb9a-42a1-99ac-e5f8cf368012";
    public final static String FINISHED = "7ca6af8b-bb9a-42a1-99ac-e5f8cf368096";
}
