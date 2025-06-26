package application;

import java.sql.*;

public class DataBaseTest {
        public static void main(String[] args) {
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            try {
                Class.forName("org.sqlite.JDBC");

                // Se connecter à la base de données
                conn = DriverManager.getConnection("jdbc:sqlite:my-database.db");
                System.out.println("Connexion à la base de données SQLite établie.");

                stmt = conn.createStatement();

                // Exécuter une requête SELECT
                //String table = "DROP TABLE words";

                //stmt.execute(table);

                // Créer un statement
                stmt = conn.createStatement();

                // Exécuter une requête SELECT
                String table = "CREATE TABLE IF NOT EXISTS words(word TEXT)";

                stmt.execute(table);

                // Afficher les résultats
                System.out.printf("Table created\n");


                Connection finalConn = conn;

                    String insert0 = "INSERT INTO words(word) VALUES(?)";
                    Long start = System.currentTimeMillis();
                    PreparedStatement pstmt = null;
                    System.out.println("Agent 00");
                    try {
                        conn.setAutoCommit(false);
                        pstmt = finalConn.prepareStatement(insert0);

                        for(int i = 0; i < 1000; i++){
                            pstmt.setString(1, "cruzeiro");
                            pstmt.addBatch();
                        }

                        pstmt.executeBatch();
                        conn.commit();

                        Long end = System.currentTimeMillis();

                        System.out.printf("Table inserted in %dms\n", end - start);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }


                Thread.sleep(1000);
                String select = "SELECT word, COUNT(*) as frequency FROM words GROUP BY word";
                rs = stmt.executeQuery(select);

                // Afficher les résultats
                while (rs.next()) {
                    String word = rs.getString("word");
                    Integer frequency = rs.getInt("frequency");

                    System.out.printf("word: %s frequency: %d", word,frequency);
                }

                finalConn.close();

            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                // Fermer le Statement
                try {
                    if (stmt != null) stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
}
