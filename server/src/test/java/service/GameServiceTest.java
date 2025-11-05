package service;

import dataaccess.*;
import handler.exceptions.BadRequestException;
import handler.exceptions.ForbiddenException;
import handler.exceptions.UnauthorizedException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;

import static org.junit.jupiter.api.Assertions.*;

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
    void testCreateGameSuccess() throws ForbiddenException, BadRequestException, UnauthorizedException, DataAccessException {
        UserData user = new UserData("tommy", "pass123", "tommy@example.com");
        AuthData auth = registerService.register(user);

        int game = gameService.createGame(auth.authToken(), "My Chess Game");

        GameData stored = gameDAO.getGame(game);
        assertEquals(game, stored.gameID());
        assertNull(stored.whiteUsername());
        assertNull(stored.blackUsername());
    }

    @Test
    void testCreateGameUnauthorized() {
        assertThrows(UnauthorizedException.class, () ->
                gameService.createGame("invalidToken", "My Chess Game"));
    }

    @Test
    void testJoinGameSuccess() throws ForbiddenException, BadRequestException, UnauthorizedException, DataAccessException {
        UserData user = new UserData("tommy", "pass123", "tommy@example.com");
        AuthData auth = registerService.register(user);

        int game = gameService.createGame(auth.authToken(), "My Chess Game");

        gameService.joinGame(auth.authToken(), game, "WHITE");

        GameData updated = gameDAO.getGame(game);
        assertEquals("tommy", updated.whiteUsername());
        assertNull(updated.blackUsername());
    }

    @Test
    void testJoinGameInvalidColor() throws ForbiddenException, BadRequestException, UnauthorizedException, DataAccessException {
        UserData user = new UserData("tommy", "pass123", "tommy@example.com");
        AuthData auth = registerService.register(user);
        int game = gameService.createGame(auth.authToken(), "My Chess Game");

        assertThrows(BadRequestException.class, () ->
                gameService.joinGame(auth.authToken(), game, "PURPLE"));
    }

    @Test
    void testJoinGameInvalidToken() {
        assertThrows(UnauthorizedException.class, () ->
                gameService.joinGame("fakeToken", 1, "WHITE"));
    }
}
