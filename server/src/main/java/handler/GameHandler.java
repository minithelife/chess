package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import model.GameData;
import service.GameService;

import java.util.List;
import java.util.Map;

public class GameHandler {
    private final Gson gson = new Gson();
    private final GameService service = new GameService();

    /** List all games */
    public void listGames(Context ctx) {
        String authToken = ctx.header("authorization");
        List<GameData> games = service.listGames(authToken); // may throw UnauthorizedException
        ctx.status(200);
        ctx.json(Map.of("games", games));
    }

    /** Create a new game */
    public void createGame(Context ctx) {
        String authToken = ctx.header("authorization");
        Map<String, String> request = gson.fromJson(ctx.body(), Map.class);
        String gameName = request.get("gameName");
        GameData game = service.createGame(authToken, gameName); // may throw UnauthorizedException or BadRequestException
        ctx.status(200);
        ctx.json(Map.of("gameID", game.gameId()));
    }

    /** Join an existing game */
    public void joinGame(Context ctx) {
        String authToken = ctx.header("authorization");
        Map<String, Object> request = gson.fromJson(ctx.body(), Map.class);
        int gameId = ((Double) request.get("gameID")).intValue();
        String playerColor = (String) request.get("playerColor");
        service.joinGame(authToken, gameId, playerColor); // may throw UnauthorizedException, BadRequestException, ForbiddenException
        ctx.status(200);
    }
}
