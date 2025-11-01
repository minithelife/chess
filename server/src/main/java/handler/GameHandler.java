package handler;

import com.google.gson.Gson;
import handler.exceptions.ForbiddenException;
import handler.exceptions.UnauthorizedException;
import io.javalin.http.Context;
import model.GameData;
import service.GameService;
import handler.exceptions.BadRequestException;

import java.util.List;
import java.util.Map;

public class GameHandler {

    private final Gson gson = new Gson();
    private final GameService service;

    public GameHandler(GameService service) {
        this.service = service;
    }

    /** List all games for a user */
    public void listGames(Context ctx) throws BadRequestException, UnauthorizedException {
        String authToken = ctx.header("authorization");
        if (authToken == null || authToken.isEmpty()) {
            throw new BadRequestException("Missing authorization header");
        }

        List<GameData> games = service.listGames(authToken);
        ctx.status(200);
        ctx.result(gson.toJson(Map.of("games", games)));
    }

    /** Create a new game */
    public void createGame(Context ctx) throws BadRequestException, UnauthorizedException {
        String authToken = ctx.header("authorization");
        if (authToken == null || authToken.isEmpty()) {
            throw new BadRequestException("Missing authorization header");
        }

        Map<String, String> request;
        try {
            request = gson.fromJson(ctx.body(), Map.class);
        } catch (Exception e) {
            throw new BadRequestException("Invalid JSON body");
        }

        String gameName = request.get("gameName");
        if (gameName == null || gameName.isEmpty()) {
            throw new BadRequestException("gameName is required");
        }

        GameData game = service.createGame(authToken, gameName);

        ctx.status(200);
        ctx.result(gson.toJson(Map.of("gameID", game.gameID())));
    }

    /** Join an existing game as WHITE or BLACK */
    public void joinGame(Context ctx) throws BadRequestException, ForbiddenException, UnauthorizedException {
        String authToken = ctx.header("authorization");
        if (authToken == null || authToken.isEmpty()) {
            throw new BadRequestException("Missing authorization header");
        }

        Map<String, Object> request;
        try {
            request = gson.fromJson(ctx.body(), Map.class);
        } catch (Exception e) {
            throw new BadRequestException("Invalid JSON body");
        }

        if (request.get("gameID") == null || request.get("playerColor") == null) {
            throw new BadRequestException("Missing gameID or playerColor");
        }

        int gameId;
        try {
            gameId = ((Number) request.get("gameID")).intValue();
        } catch (ClassCastException e) {
            throw new BadRequestException("gameID must be a number");
        }

        String playerColor = request.get("playerColor").toString();

        service.joinGame(authToken, gameId, playerColor);

        ctx.status(200);
    }
}
