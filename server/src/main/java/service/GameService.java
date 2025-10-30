package service;

import dao.AuthDAO;
import dao.GameDAO;
import handler.exceptions.BadRequestException;
import handler.exceptions.ForbiddenException;
import handler.exceptions.UnauthorizedException;
import model.GameData;

import java.util.List;

public class GameService extends Service{

//    private final AuthDAO authDAO = new AuthDAO();
//    private final GameDAO gameDAO = new GameDAO();

    public List<GameData> listGames(String authToken) {
        var auth = authDAO.getAuth(authToken);
        if (auth == null) throw new UnauthorizedException("unauthorized");

        return gameDAO.getAllGames();
    }

    public GameData createGame(String authToken, String gameName) {
        var auth = authDAO.getAuth(authToken);
        if (auth == null) throw new UnauthorizedException("unauthorized");
        if (gameName == null || gameName.isEmpty()) throw new BadRequestException("bad request");

        int gameId = gameDAO.getNextGameId();
        GameData game = new GameData(gameId, gameName, auth.username(), null);
        gameDAO.createGame(game);
        return game;
    }

    public void joinGame(String authToken, int gameId, String playerColor) {
        var auth = authDAO.getAuth(authToken);
        if (auth == null) throw new UnauthorizedException("unauthorized");

        GameData game = gameDAO.getGame(gameId);
        if (game == null) throw new BadRequestException("bad request");

        if ("WHITE".equalsIgnoreCase(playerColor)) {
            if (game.white() != null) throw new ForbiddenException("already taken");
            game = game.withWhite(auth.username());
        } else if ("BLACK".equalsIgnoreCase(playerColor)) {
            if (game.black() != null) throw new ForbiddenException("already taken");
            game = game.withBlack(auth.username());
        } else {
            throw new BadRequestException("bad request");
        }

        gameDAO.updateGame(game);
    }
}
