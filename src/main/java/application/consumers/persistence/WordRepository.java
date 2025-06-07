package application.consumers.persistence;

import application.communication.CommunicationChanel;
import application.communication.Queues;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class WordRepository {

    private static Connection connection;
    private static Connection getConnection() {
        if (connection == null) {
            try {

                connection = DriverManager.getConnection("jdbc:sqlite:romulus");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        return connection;
    }

    public static void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void createTable() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try(Connection connection = DriverManager.getConnection("jdbc:sqlite:romulus")) {
            try(Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA synchronous = OFF");
                statement.execute("PRAGMA journal_mode = MEMORY");
                final String drop = "DROP TABLE IF EXISTS words";
                final String create = "CREATE TABLE IF NOT EXISTS words (word TEXT PRIMARY KEY, " +
                        "frequency INTEGER DEFAULT 1)";
                statement.addBatch(drop);
                statement.addBatch(create);
                statement.executeBatch();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void save(CommunicationChanel chanel, Queues queues) {
        Long startTime = System.currentTimeMillis();

        createTable();
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try(Connection connection = DriverManager.getConnection("jdbc:sqlite:romulus")) {

                final String insert = "INSERT INTO words(word, frequency) " +
                        "VALUES (?, ?) " +
                        "ON CONFLICT(word) DO UPDATE SET frequency = frequency + 1;";

                Map<String, Integer> words;
                connection.setAutoCommit(false);
            try(PreparedStatement statement = connection.prepareStatement(insert)) {
                while ((words = queues.wordsToPersist.poll()) != null || !chanel.persistenceFinished.get()) {

                    if (words == null) continue;
                    Long start = System.currentTimeMillis();
                        int i = 0;
                        for (Map.Entry<String,Integer> word : words.entrySet()) {
                            statement.setString(1, word.getKey());
                            statement.setInt(2, word.getValue());
                            statement.addBatch();
                        }

                        System.out.println("Entering in Batch");
                        statement.executeBatch();
                        connection.commit();

                        System.out.println("Exiting of Batch");


                    Long end = System.currentTimeMillis();

                    System.out.println("time : "+ (end - start) + " of "+ words.size() + " words");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Long endTime = System.currentTimeMillis();

        System.out.printf("[TIME] %d",endTime - startTime);
    }

    public static Map<String, Integer> getAll(final String database) {
        try(Connection connection = DriverManager.getConnection("jdbc:sqlite:"+database)) {
            try(Statement statement = connection.createStatement()) {
                final String select = "SELECT word, COUNT(*) as frequency FROM words GROUP BY word";
                ResultSet resultSet = statement.executeQuery(select);

                Map<String, Integer> words = new HashMap<>();

                while (resultSet.next()) {
                    String word = resultSet.getString("word");
                    Integer frequency = resultSet.getInt("frequency");

                    words.put(word,frequency);
                }

                return words;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
