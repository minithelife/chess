package dataaccess;

import model.AuthData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class MySqlAuthDAO implements AuthDAO {

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        String sql = "INSERT INTO auth (token, username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, auth.authToken());
            stmt.setString(2, auth.username());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create auth token", e);
        }
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        String sql = "SELECT * FROM auth WHERE token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(
                            rs.getString("token"),
                            rs.getString("username")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get auth token", e);
        }
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        String sql = "DELETE FROM auth WHERE token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete auth token", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM auth";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear auth tokens", e);
        }
    }
}
