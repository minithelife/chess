package server;

import com.google.gson.*;
import dataaccess.*;
import model.*;
import service.*;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.*;

public class Server {

    private final Javalin javalin;
    private final Gson gson = new Gson();
    private final DataAccess dao;
    private final UserService userService;
    private final GameService gameService;

    public Server() {
        dao = new InMemoryDataAccess();
        userService = new UserService(dao);
        gameService = new GameService(dao);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // endpoints
        javalin.delete("/db", this::handleClear);

        javalin.post("/user", this::handleRegister);
        javalin.post("/session", this::handleLogin);
        javalin.delete("/session", this::handleLogout);

        javalin.get("/game", this::handleListGames);
        javalin.post("/game", this::handleCreateGame);
        javalin.put("/game", this::handleJoinGame);

        // global exception mapping for DataAccessException
        javalin.exception(UserService.BadRequestException.class, (e, ctx) -> {
            ctx.status(400).json(Map.of("message", "Error: bad request"));
        });
        javalin.exception(UserService.UnauthorizedException.class, (e, ctx) -> {
            ctx.status(401).json(Map.of("message", "Error: unauthorized"));
        });
        javalin.exception(UserService.ForbiddenException.class, (e, ctx) -> {
            ctx.status(403).json(Map.of("message", "Error: already taken"));
        });
        javalin.exception(DataAccessException.class, (e, ctx) -> {
            ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    // ---- Handlers ----

    private void handleClear(Context ctx) {
        try {
            dao.clear();
            ctx.status(200).json(Map.of());
        } catch (Exception e) {
            ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    private void handleRegister(Context ctx) throws DataAccessException {
        var req = gson.fromJson(ctx.body(), RegisterRequest.class);
        var result = userService.register(req);
        ctx.status(200).json(Map.of("username", result.username(), "authToken", result.authToken()));
    }

    private void handleLogin(Context ctx) throws DataAccessException {
        var req = gson.fromJson(ctx.body(), LoginRequest.class);
        var result = userService.login(req);
        ctx.status(200).json(Map.of("username", result.username(), "authToken", result.authToken()));
    }

    private void handleLogout(Context ctx) throws DataAccessException {
        String token = ctx.header("authorization");
        userService.logout(token);
        ctx.status(200).json(Map.of());
    }

    private void handleCreateGame(Context ctx) throws DataAccessException {
        String token = ctx.header("authorization");
        String username = userService.authenticate(token);
        var req = gson.fromJson(ctx.body(), CreateGameRequest.class);
        var res = gameService.createGame(req, username);
        ctx.status(200).json(Map.of("gameID", res.gameID()));
    }

    private void handleJoinGame(Context ctx) throws DataAccessException {
        String token = ctx.header("authorization");
        String username = userService.authenticate(token);
        var req = gson.fromJson(ctx.body(), JoinGameRequest.class);
        gameService.joinGame(req, username);
        ctx.status(200).json(Map.of());
    }

    private void handleListGames(Context ctx) throws DataAccessException {
        String token = ctx.header("authorization");
        String username = userService.authenticate(token);
        if (username == null) {
            ctx.status(401).json(Map.of("message", "Error: unauthorized"));
            return;
        }
        var list = gameService.listGames();
        // transform to expected JSON shape
        List<Map<String, Object>> out = new ArrayList<>();
        for (var e : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("gameID", e.gameID());
            m.put("whiteUsername", e.whiteUsername());
            m.put("blackUsername", e.blackUsername());
            m.put("gameName", e.gameName());
            out.add(m);
        }
        ctx.status(200).json(Map.of("games", out));
    }

    // ---- request/result DTOs ----
    public static record RegisterRequest(String username, String password, String email) {}
    public static record LoginRequest(String username, String password) {}
    public static record AuthResult(String username, String authToken) {}
    public static record CreateGameRequest(String gameName) {}
    public static record CreateGameResult(int gameID) {}
    public static record JoinGameRequest(String playerColor, Integer gameID) {}
}
