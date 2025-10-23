package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.InMemoryDataAccess;
import io.javalin.Javalin;
import io.javalin.http.Context;
import model.GameData;
import requests.*;
import results.*;
import service.GameService;
import service.UserService;
import service.UserService.BadRequestException;
import service.UserService.UnauthorizedException; // Import for clarity

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // ---- Endpoints ----
        javalin.delete("/db", this::handleClear);
        javalin.post("/user", this::handleRegister);
        javalin.post("/session", this::handleLogin);
        javalin.delete("/session", this::handleLogout);
        javalin.get("/game", this::handleListGames);
        javalin.post("/game", this::handleCreateGame);
        javalin.put("/game", this::handleJoinGame);

        // ---- Exception mapping ----
        javalin.exception(UserService.BadRequestException.class, (e, ctx) ->
                ctx.status(400).json(Map.of("message", "Error: bad request")));
        javalin.exception(UserService.UnauthorizedException.class, (e, ctx) ->
                ctx.status(401).json(Map.of("message", "Error: unauthorized")));
        javalin.exception(UserService.ForbiddenException.class, (e, ctx) ->
                ctx.status(403).json(Map.of("message", "Error: already taken")));
        javalin.exception(DataAccessException.class, (e, ctx) ->
                ctx.status(500).json(Map.of("message", "Error: " + e.getMessage())));
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

    // --- EDITED METHOD: Enforces 400 Bad Request if gameName is missing. ---
    private void handleCreateGame(Context ctx) throws DataAccessException {
        String token = ctx.header("authorization");
        // userService.authenticate now throws 401 if token is bad
        String username = userService.authenticate(token);

        var req = gson.fromJson(ctx.body(), CreateGameRequest.class);

        // Check for 400 Bad Request
        if (req == null || req.gameName() == null || req.gameName().isBlank()) {
            throw new BadRequestException("Bad request: missing gameName");
        }

        var res = gameService.createGame(req.gameName(), username);
        ctx.status(200).json(Map.of("gameID", res.gameID()));
    }
    // ----------------------------------------------------------------------

    private void handleJoinGame(Context ctx) throws DataAccessException {
        String token = ctx.header("authorization");
        // userService.authenticate now throws 401 if token is bad
        String username = userService.authenticate(token);

        var req = gson.fromJson(ctx.body(), JoinGameRequest.class);
        gameService.joinGame(req.gameID(), username, req.playerColor());
        ctx.status(200).json(Map.of());
    }

    // --- EDITED METHOD: Removed manual 401 check, now handled by authenticate. ---
    private void handleListGames(Context ctx) throws DataAccessException {
        String token = ctx.header("authorization");
        // userService.authenticate now throws 401 if token is bad
        String username = userService.authenticate(token);

        // The rest of the logic is fine
        var list = gameService.listGames();

        List<Map<String, Object>> out = new ArrayList<>();
        for (GameData e : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("gameID", e.gameId());
            m.put("whiteUsername", e.white());
            m.put("blackUsername", e.black());
            m.put("gameName", e.name());
            out.add(m);
        }
        ctx.status(200).json(Map.of("games", out));
    }
    // -----------------------------------------------------------------------------
}