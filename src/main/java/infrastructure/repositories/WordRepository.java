package infrastructure.repositories;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class WordRepository implements contracts.WordRepository {

    private Connection connection;
    private Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection("jdbc:sqlite:romulus");
        }

        return connection;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public WordRepository() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void createTable() {
        try(Connection connection = DriverManager.getConnection("jdbc:sqlite:romulus")) {
            try(Statement statement = connection.createStatement()) {
                final String drop = "DROP TABLE IF EXISTS words";
                final String create = "CREATE TABLE IF NOT EXISTS words (word TEXT NOT NULL)";
                statement.addBatch(drop);
                statement.addBatch(create);
                statement.executeBatch();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Queue<String> words) {
        try {
                final String insert = "INSERT INTO words(word) VALUES(?)";
                Connection connection = getConnection();
                try(PreparedStatement statement = connection.prepareStatement(insert)) {
                    for (String word : words) {
                        statement.setString(1, word);
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Map<String, Integer> getAll() {
        try(Connection connection = DriverManager.getConnection("jdbc:sqlite:romulus")) {
            try(Statement statement = connection.createStatement()) {
                final String select = "SELECT word, SUM(*) as frequency FROM words GROUP BY word";
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
