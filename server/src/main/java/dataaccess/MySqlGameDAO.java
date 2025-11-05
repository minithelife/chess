package dataaccess;

import com.google.gson.Gson;
import model.GameData;
import chess.ChessGame;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlGameDAO implements GameDAO {

    private final Gson gson = new Gson();

    @Override
    public int createGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO games (game_name, white_username, black_username, game_state) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, game.gameName());
            stmt.setString(2, game.whiteUsername());
            stmt.setString(3, game.blackUsername());
            stmt.setString(4, gson.toJson(game.game()));
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next())  {
                    int id = keys.getInt(1);
                    return id;  // Add setter or create a new GameData with ID if immutable
                } else{
                    throw new DataAccessException("Game ID not found");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create game", e);
        }
    }

    @Override
    public GameData getGame(int gameId) throws DataAccessException {
        String sql = "SELECT * FROM games WHERE game_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ChessGame chessGame = gson.fromJson(rs.getString("game_state"), ChessGame.class);
                    return new GameData(
                            rs.getInt("game_id"),
                            rs.getString("game_name"),
                            rs.getString("white_username"),
                            rs.getString("black_username"),
                            chessGame
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get game", e);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE games SET white_username = ?, black_username = ?, game_state = ? WHERE game_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, gson.toJson(game.game()));
            stmt.setInt(4, game.gameID());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update game", e);
        }
    }

    @Override
    public List<GameData> getAllGames() throws DataAccessException {
        List<GameData> games = new ArrayList<>();
        String sql = "SELECT * FROM games";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ChessGame chessGame = gson.fromJson(rs.getString("game_state"), ChessGame.class);
                games.add(new GameData(
                        rs.getInt("game_id"),
                        rs.getString("game_name"),
                        rs.getString("white_username"),
                        rs.getString("black_username"),
                        chessGame
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get all games", e);
        }
        return games;
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM games";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear games", e);
        }
    }
}
