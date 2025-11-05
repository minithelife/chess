package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameDAOTest {

    private static MySqlGameDAO gameDao;
    private static MySqlUserDAO userDao;

    @BeforeAll
    public static void init() {
        gameDao = new MySqlGameDAO();
        userDao = new MySqlUserDAO();
    }

    @BeforeEach
    public void setup() throws DataAccessException {
        // Clear both tables to have a clean state
        gameDao.clear();
        userDao.clear();

        // Insert users needed for foreign key constraints
        UserData user1 = new UserData("whitePlayer", "pass123", "white@example.com");
        UserData user2 = new UserData("blackPlayer", "pass456", "black@example.com");

        userDao.createUser(user1);
        userDao.createUser(user2);
    }

    @Test
    @Order(1)
    @DisplayName("Create and get game success")
    public void testCreateAndGetGameSuccess() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(0, "TestGame", "whitePlayer", "blackPlayer", chessGame);

        int id = gameDao.createGame(game);
        Assertions.assertTrue(id > 0, "Game ID should be positive");

        GameData fetchedGame = gameDao.getGame(id);
        Assertions.assertNotNull(fetchedGame, "Fetched game should not be null");
        Assertions.assertEquals("TestGame", fetchedGame.gameName());
        Assertions.assertEquals("whitePlayer", fetchedGame.whiteUsername());
        Assertions.assertEquals("blackPlayer", fetchedGame.blackUsername());
    }

    @Test
    @Order(2)
    @DisplayName("Create game fails with invalid usernames (foreign key violation)")
    public void testCreateGameFailInvalidUsernames() {
        ChessGame chessGame = new ChessGame();
        // usernames that do NOT exist in users table
        GameData game = new GameData(0, "InvalidGame", "nonexistent1", "nonexistent2", chessGame);

        Assertions.assertThrows(DataAccessException.class, () -> {
            gameDao.createGame(game);
        });
    }

    @Test
    @Order(3)
    @DisplayName("Get game returns null for non-existent ID")
    public void testGetGameNotFound() throws DataAccessException {
        GameData game = gameDao.getGame(9999);  // assuming this ID does not exist
        Assertions.assertNull(game, "Game should be null if not found");
    }

    @Test
    @Order(4)
    @DisplayName("Update game success")
    public void testUpdateGameSuccess() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(0, "UpdateGame", "whitePlayer", "blackPlayer", chessGame);

        int id = gameDao.createGame(game);
        Assertions.assertTrue(id > 0);

        // Modify the game data
        GameData updatedGame = new GameData(id, "UpdatedName", "whitePlayer", "blackPlayer", chessGame);
        gameDao.updateGame(updatedGame);

        GameData fetched = gameDao.getGame(id);
        Assertions.assertNotNull(fetched);
        Assertions.assertEquals("UpdatedName", fetched.gameName());
    }

    @Test
    @Order(5)
    @DisplayName("Update game fails with invalid ID")
    public void testUpdateGameFailInvalidId() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(9999, "NoGame", "whitePlayer", "blackPlayer", chessGame);

        Assertions.assertDoesNotThrow(() -> {
            // The update may do nothing or throw - depends on implementation
            gameDao.updateGame(game);
        });
        // You could fetch and check that it wasn't created:
        Assertions.assertNull(gameDao.getGame(9999));
    }

    @Test
    @Order(6)
    @DisplayName("Get all games returns all inserted games")
    public void testGetAllGames() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        GameData game1 = new GameData(0, "Game1", "whitePlayer", "blackPlayer", chessGame);
        GameData game2 = new GameData(0, "Game2", "whitePlayer", "blackPlayer", chessGame);

        gameDao.createGame(game1);
        gameDao.createGame(game2);

        List<GameData> games = gameDao.getAllGames();
        Assertions.assertEquals(2, games.size());
    }

    @Test
    @Order(7)
    @DisplayName("Negative: get all games returns empty list when no games exist")
    public void testGetAllGamesEmpty() throws DataAccessException {
        // Make sure games table is empty
        gameDao.clear();

        List<GameData> games = gameDao.getAllGames();
        Assertions.assertNotNull(games);
        Assertions.assertTrue(games.isEmpty(), "Games list should be empty when no games exist");
    }

    @Test
    @Order(8)
    @DisplayName("Clear games deletes all entries")
    public void testClearGames() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(0, "GameToClear", "whitePlayer", "blackPlayer", chessGame);

        gameDao.createGame(game);
        gameDao.clear();

        List<GameData> games = gameDao.getAllGames();
        Assertions.assertTrue(games.isEmpty(), "Games table should be empty after clear");
    }
}
