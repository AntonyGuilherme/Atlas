import java.sql.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataBaseTest {
        public static void main(String[] args) {
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            try {
                AtomicBoolean closed = new AtomicBoolean(false);
                // Charger la classe JDBC
                Class.forName("org.sqlite.JDBC");

                // Se connecter à la base de données
                conn = DriverManager.getConnection("jdbc:sqlite:my-database.db");
                System.out.println("Connexion à la base de données SQLite établie.");

                // Créer un statement
                stmt = conn.createStatement();

                // Exécuter une requête SELECT
                String wordsAgent0 = "CREATE TABLE IF NOT EXISTS wordsAgent0 (frequency INTEGER, word TEXT NOT NULL)";
                String wordsAgent1 = "CREATE TABLE IF NOT EXISTS wordsAgent1 (frequency INTEGER, word TEXT NOT NULL)";

                stmt.execute(wordsAgent0);
                stmt.execute(wordsAgent1);

                // Afficher les résultats
                System.out.printf("Table created\n");


                Connection finalConn = conn;
                new Thread(() ->{
                    String insert0 = "INSERT INTO wordsAgent0(frequency, word) VALUES(?,?)";
                    Long start = System.currentTimeMillis();
                    PreparedStatement pstmt = null;
                    System.out.println("Agent 00");
                    try {
                        pstmt = finalConn.prepareStatement(insert0);
                        for(int i = 0; i < 1000000; i++){
                            pstmt.setInt(1, i+1);
                            pstmt.setString(2, "cruzeiro");
                            pstmt.addBatch();
                        }

                        pstmt.executeUpdate();

                        Long end = System.currentTimeMillis();

                        System.out.printf("Table inserted in %dms\n", end - start);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }finally {
                        if (closed.getAndSet(true)) {
                            try {
                                finalConn.close();
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }).start();

                new Thread(() ->{
                    String insert1 = "INSERT INTO wordsAgent0(frequency, word) VALUES(?,?)";
                    Long start = System.currentTimeMillis();
                    PreparedStatement pstmt = null;
                    try {
                        pstmt = finalConn.prepareStatement(insert1);
                        System.out.println("Agent 01");
                        for(int i = 0; i < 1000000; i++){
                            pstmt.setInt(1, i+1);
                            pstmt.setString(2, "cruzeiro");
                            pstmt.addBatch();
                        }

                        pstmt.executeUpdate();

                        Long end = System.currentTimeMillis();

                        System.out.printf("Table inserted in %dms\n", end - start);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    finally {
                        if (closed.getAndSet(true)) {
                            try {
                                finalConn.close();
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }).start();


//                String select = "SELECT word, SUM(frequency) as frequency FROM words GROUP BY word";
//                rs = stmt.executeQuery(select);
//
//                // Afficher les résultats
//                while (rs.next()) {
//                    String word = rs.getString("word");
//                    Integer frequency = rs.getInt("frequency");
//
//                    System.out.printf("word: %s frequency: %d", word,frequency);
//                }

            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
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
