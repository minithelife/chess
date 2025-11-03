package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MySqlDatabaseInitializer {

    public static void createTables() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            String userTable = """
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(50) PRIMARY KEY,
                    password VARCHAR(60) NOT NULL,
                    email VARCHAR(100) NOT NULL
                );
                """;

            String authTable = """
                CREATE TABLE IF NOT EXISTS auth (
                    token VARCHAR(36) PRIMARY KEY,
                    username VARCHAR(50),
                    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
                );
                """;

            String gameTable = """
                CREATE TABLE IF NOT EXISTS games (
                    game_id INT PRIMARY KEY AUTO_INCREMENT,
                    game_name VARCHAR(100),
                    white_username VARCHAR(50),
                    black_username VARCHAR(50),
                    game_state TEXT,
                    FOREIGN KEY (white_username) REFERENCES users(username) ON DELETE SET NULL,
                    FOREIGN KEY (black_username) REFERENCES users(username) ON DELETE SET NULL
                );
                """;

            stmt.execute(userTable);
            stmt.execute(authTable);
            stmt.execute(gameTable);

        } catch (SQLException e) {
            throw new DataAccessException("Failed to create tables", e);
        }
    }
}
