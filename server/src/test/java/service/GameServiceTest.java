package service;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import handler.exceptions.BadRequestException;
import handler.exceptions.ForbiddenException;
import handler.exceptions.UnauthorizedException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    private GameService service;
    private String token;

    @BeforeEach
    public void setup() {
        new ClearService().clear();
        service = new GameService();
        AuthData auth = new RegisterService().register(new UserData("tommy", "pass123", "tommy@email.com"));
        token = auth.token();
    }

    @Test
    public void testCreateGameSuccess() {
        GameData game = service.createGame(token, "Chess Battle");
        assertNotNull(game);
        assertEquals("Chess Battle", game.name());
        assertEquals("tommy", game.white());
        assertNull(game.black());
    }

    @Test
    public void testCreateGameBadRequest() {
        assertThrows(BadRequestException.class, () -> service.createGame(token, ""));
        assertThrows(BadRequestException.class, () -> service.createGame(token, null));
    }

    @Test
    public void testListGamesSuccess() {
        service.createGame(token, "Chess Battle");
        List<GameData> games = service.listGames(token);
        assertEquals(1, games.size());
    }

    @Test
    public void testJoinGameSuccess() {
        GameData game = service.createGame(token, "Chess Battle");
        // register another user
        String token2 = new RegisterService().register(new UserData("bob", "pass", "bob@email.com")).token();
        service.joinGame(token2, game.gameId(), "BLACK");

        List<GameData> games = service.listGames(token);
        GameData updated = games.get(0);
        assertEquals("tommy", updated.white());
        assertEquals("bob", updated.black());
    }

    @Test
    public void testJoinGameAlreadyTaken() {
        GameData game = service.createGame(token, "Chess Battle");
        // register another user
        String token2 = new RegisterService().register(new UserData("bob", "pass", "bob@email.com")).token();
        service.joinGame(token2, game.gameId(), "BLACK");

        assertThrows(ForbiddenException.class, () -> service.joinGame(new RegisterService().register(new UserData("jim", "pass", "jim@email.com")).token(), game.gameId(), "BLACK"));
    }

    @Test
    public void testJoinGameBadRequest() {
        assertThrows(BadRequestException.class, () -> service.joinGame(token, 999, "WHITE")); // non-existent game
        assertThrows(BadRequestException.class, () -> service.joinGame(token, 1, "GREEN")); // invalid color
    }

    @Test
    public void testUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> service.listGames("invalid"));
        assertThrows(UnauthorizedException.class, () -> service.createGame("invalid", "Chess"));
        assertThrows(UnauthorizedException.class, () -> service.joinGame("invalid", 1, "WHITE"));
    }
}
