package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameServiceTest {

    private AuthDAO authDAO;
    private UserDAO userDAO;
    private GameDAO gameDAO;

    private RegisterService registerService;
    private LoginService loginService;
    private GameService gameService;

    @BeforeEach
    void setup() {
        // Initialize in-memory DAOs
        authDAO = new InMemoryAuth();
        userDAO = new InMemoryUser();
        gameDAO = new InMemoryGame();

        // Initialize services
        registerService = new RegisterService(userDAO, authDAO);
        loginService = new LoginService(userDAO, authDAO);
        gameService = new GameService(authDAO, gameDAO, userDAO);
    }

    @Test
    void testCreateGameSuccess() {
        // 1. Register user
        UserData user = new UserData("tommy", "pass123", "tommy@example.com");
        AuthData auth = registerService.register(user);

        // 2. Create a game
        GameData game = gameService.createGame(auth.authToken(), "My Chess Game");

        // 3. Assert game exists and has correct ID
        GameData stored = gameDAO.getGame(game.gameID());
        assertEquals(game.gameID(), stored.gameID());
        assertEquals(null, stored.whiteUsername()); // not joined yet
        assertEquals(null, stored.blackUsername());
    }

    @Test
    void testJoinGameSuccess() {
        // 1. Register user
        UserData user = new UserData("tommy", "pass123", "tommy@example.com");
        AuthData auth = registerService.register(user);

        // 2. Create a game
        GameData game = gameService.createGame(auth.authToken(), "My Chess Game");

        // 3. Join the game as WHITE
        gameService.joinGame(auth.authToken(), game.gameID(), "WHITE");

        // 4. Assert that the WHITE player is set correctly
        GameData updated = gameDAO.getGame(game.gameID());
        assertEquals("tommy", updated.whiteUsername());
        assertEquals(null, updated.blackUsername());
    }
}
