package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import handler.exceptions.BadRequestException;
import handler.exceptions.ForbiddenException;
import handler.exceptions.UnauthorizedException;
import model.GameData;
import chess.ChessGame;

import java.util.List;

public class GameService {

    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final UserDAO userDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO, UserDAO userDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.userDAO = userDAO;
    }

    /** Returns a list of all games, only if authToken is valid */
    public List<GameData> listGames(String authToken) {
        var auth = authDAO.getAuth(authToken);
        if (auth == null) throw new UnauthorizedException("unauthorized");
        return gameDAO.getAllGames();
    }

    /** Creates a new game and returns it */
    public GameData createGame(String authToken, String gameName) {
        var auth = authDAO.getAuth(authToken);
        if (auth == null) throw new UnauthorizedException("unauthorized");
        if (gameName == null || gameName.isEmpty()) throw new BadRequestException("bad request");

        int gameID = gameDAO.getNextGameId();
        ChessGame chessGame = new ChessGame(); // initialize a new chess game
        GameData game = new GameData(gameID, gameName, null, null, chessGame);
        gameDAO.createGame(game);
        return game;
    }

    /** Joins a player to a game in the requested color */
    public void joinGame(String authToken, int gameId, String playerColor) {
        var auth = authDAO.getAuth(authToken);
        if (auth == null) throw new UnauthorizedException("unauthorized");

        GameData game = gameDAO.getGame(gameId);
        if (game == null) throw new BadRequestException("bad request");

        if ("WHITE".equalsIgnoreCase(playerColor)) {
            if (game.whiteUsername() != null) throw new ForbiddenException("already taken");
            game = game.withWhite(auth.username());
        } else if ("BLACK".equalsIgnoreCase(playerColor)) {
            if (game.blackUsername() != null) throw new ForbiddenException("already taken");
            game = game.withBlack(auth.username());
        } else {
            throw new BadRequestException("bad request");
        }

        gameDAO.updateGame(game);
    }
}
