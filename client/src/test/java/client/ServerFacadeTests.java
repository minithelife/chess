package client;

import org.junit.jupiter.api.*;
import server.Server;
import model.AuthData;
import model.GameData;
import model.responses.ListGamesResponse;
import model.requests.*;

public class ServerFacadeTests {

    private static Server server;
    private static serverfacade.ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
        System.out.println("Started test HTTP server on port " + port);
        facade = new serverfacade.ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clear() throws Exception {
        facade.clear();
    }

    // ─────────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────────
    @Test
    public void registerPositive() throws Exception {
        var auth = facade.register(new RegisterRequest("alice", "pw", "a@b.com"));
        Assertions.assertNotNull(auth);
        Assertions.assertEquals("alice", auth.username());
        Assertions.assertNotNull(auth.authToken());
    }

    @Test
    public void registerNegativeDuplicate() throws Exception {
        facade.register(new RegisterRequest("bob", "pw", "b@b.com"));
        Assertions.assertThrows(Exception.class, () ->
                facade.register(new RegisterRequest("bob", "pw", "b@b.com")));
    }


    // ─────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────
    @Test
    public void loginPositive() throws Exception {
        facade.register(new RegisterRequest("carol", "pw", "c@c.com"));
        var auth = facade.login(new LoginRequest("carol", "pw"));
        Assertions.assertEquals("carol", auth.username());
    }

    @Test
    public void loginNegativeWrongPassword() {
        Assertions.assertThrows(Exception.class, () ->
                facade.login(new LoginRequest("carol", "wrong")));
    }

    @Test
    public void loginNegativeUnknownUser() {
        Assertions.assertThrows(Exception.class, () ->
                facade.login(new LoginRequest("nosuchuser", "pw")));
    }

    // ─────────────────────────────────────────────
    // LOGOUT
    // ─────────────────────────────────────────────
    @Test
    public void logoutPositive() throws Exception {
        var auth = facade.register(new RegisterRequest("dave", "pw", "d@d.com"));
        facade.logout(auth.authToken());
        Assertions.assertThrows(Exception.class, () ->
                facade.logout(auth.authToken())); // cannot logout twice
    }

    @Test
    public void logoutNegativeInvalidToken() {
        Assertions.assertThrows(Exception.class, () ->
                facade.logout("invalid-token"));
    }

    // ─────────────────────────────────────────────
    // CLEAR
    // ─────────────────────────────────────────────
    @Test
    public void clearPositive() throws Exception {
        facade.register(new RegisterRequest("eve", "pw", "e@e.com"));
        facade.clear();
        // login should now fail
        Assertions.assertThrows(Exception.class, () ->
                facade.login(new LoginRequest("eve", "pw")));
    }

    // ─────────────────────────────────────────────
    // CREATE GAME
    // ─────────────────────────────────────────────
    @Test
    public void createGamePositive() throws Exception {
        var auth = facade.register(new RegisterRequest("frank", "pw", "f@f.com"));
        var createRes = facade.createGame(auth.authToken(), new CreateGameRequest("Game1"));
        Assertions.assertTrue(createRes.gameID() > 0);
    }

    @Test
    public void createGameNegativeInvalidAuth() {
        Assertions.assertThrows(Exception.class, () ->
                facade.createGame("badtoken", new CreateGameRequest("BadGame")));
    }

    // ─────────────────────────────────────────────
    // LIST GAMES
    // ─────────────────────────────────────────────
    @Test
    public void listGamesPositive() throws Exception {
        var auth = facade.register(new RegisterRequest("henry", "pw", "h@h.com"));
        facade.createGame(auth.authToken(), new CreateGameRequest("L1"));
        facade.createGame(auth.authToken(), new CreateGameRequest("L2"));

        ListGamesResponse resp = facade.listGames(auth.authToken());
        Assertions.assertEquals(2, resp.games().size());
    }

    @Test
    public void listGamesNegativeInvalidAuth() {
        Assertions.assertThrows(Exception.class, () ->
                facade.listGames("badtoken"));
    }

    // ─────────────────────────────────────────────
    // JOIN GAME
    // ─────────────────────────────────────────────
    @Test
    public void joinGamePositive() throws Exception {
        var auth = facade.register(new RegisterRequest("gail", "pw", "g@g.com"));
        var gid = facade.createGame(auth.authToken(), new CreateGameRequest("JoinMe"));

        facade.joinGame(auth.authToken(),
                new JoinGameRequest("WHITE", gid.gameID()));

        // second join as same color should fail
        Assertions.assertThrows(Exception.class, () ->
                facade.joinGame(auth.authToken(),
                        new JoinGameRequest("WHITE", gid.gameID())));
    }

    @Test
    public void joinGameNegativeInvalidColor() throws Exception {
        var auth = facade.register(new RegisterRequest("ivy", "pw", "i@i.com"));
        var gid = facade.createGame(auth.authToken(), new CreateGameRequest("BadColor"));

        Assertions.assertThrows(Exception.class, () ->
                facade.joinGame(auth.authToken(),
                        new JoinGameRequest("PURPLE", gid.gameID())));
    }

    @Test
    public void joinGameNegativeInvalidAuth() {
        Assertions.assertThrows(Exception.class, () ->
                facade.joinGame("badtoken",
                        new JoinGameRequest("WHITE", 1)));
    }

    @Test
    public void joinGameNegativeInvalidGame() throws Exception {
        var auth = facade.register(new RegisterRequest("kate", "pw", "k@k.com"));
        Assertions.assertThrows(Exception.class, () ->
                facade.joinGame(auth.authToken(),
                        new JoinGameRequest("WHITE", 9999)));
    }

    // ─────────────────────────────────────────────
    // GET GAME
    // ─────────────────────────────────────────────
    @Test
    public void getGamePositive() throws Exception {
        var auth = facade.register(new RegisterRequest("leo", "pw", "l@l.com"));
        var gid = facade.createGame(auth.authToken(), new CreateGameRequest("GG"));
        GameData gd = facade.getGame(gid.gameID(), auth.authToken());
        Assertions.assertEquals("GG", gd.gameName());
    }

    @Test
    public void getGameNegativeInvalidAuth() {
        Assertions.assertThrows(Exception.class, () ->
                facade.getGame(1, "invalid"));
    }
}

