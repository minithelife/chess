
package service;

import chess.InvalidMoveException;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import handler.exceptions.BadRequestException;
import handler.exceptions.ForbiddenException;
import handler.exceptions.UnauthorizedException;
import model.GameData;
import chess.ChessGame;
import chess.ChessMove;

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
    /** Returns the username associated with an auth token */
    public String getUsernameForAuth(String authToken) throws DataAccessException, UnauthorizedException {
        var auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("unauthorized");
        }
        return auth.username();
    }

    /** Returns a list of all games, only if authToken is valid */
    public List<GameData> listGames(String authToken) throws UnauthorizedException, DataAccessException {
        var auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("unauthorized");
        }
        return gameDAO.getAllGames();
    }

    /** Creates a new game and returns its ID */
    public int createGame(String authToken, String gameName)
            throws BadRequestException, UnauthorizedException, DataAccessException {

        var auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("unauthorized");
        }

        if (gameName == null || gameName.isEmpty()) {
            throw new BadRequestException("bad request");
        }

        ChessGame chessGame = new ChessGame();

        // ✅ CORRECT ORDER
        GameData game = new GameData(
                0,
                gameName,    // ✅ gameName
                null,        // whiteUsername
                null,        // blackUsername
                chessGame
        );


        return gameDAO.createGame(game);
    }


    /** Joins a player to a game in the requested color */
    public GameData joinGame(String authToken, int gameId, String playerColor) throws UnauthorizedException, BadRequestException, ForbiddenException, DataAccessException {
        var auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("unauthorized");
        }

        GameData game = gameDAO.getGame(gameId);
        if (game == null) {
            throw new BadRequestException("bad request");
        }

        if ("WHITE".equalsIgnoreCase(playerColor)) {
            if (game.whiteUsername() != null) {
                throw new ForbiddenException("already taken");
            }
            game = game.withWhite(auth.username());
        } else if ("BLACK".equalsIgnoreCase(playerColor)) {
            if (game.blackUsername() != null) {
                throw new ForbiddenException("already taken");
            }
            game = game.withBlack(auth.username());
        } else {
            throw new BadRequestException("bad request");
        }

        gameDAO.updateGame(game);
        return game;
    }

    /** Returns a single game by gameId (no auth check) */
    public GameData getGame(String authToken, int gameId)
            throws UnauthorizedException, DataAccessException {

        var auth = authDAO.getAuth(authToken);
        if (auth == null) throw new UnauthorizedException("unauthorized");

        GameData game = gameDAO.getGame(gameId);
        if (game == null) {
            throw new DataAccessException("Error: game does not exist");
        }

        return game;
    }


    /** Returns a game by authToken + gameId (auth check) */
    public GameData getGameState(int gameId, String authToken) throws UnauthorizedException, DataAccessException {
        var auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("unauthorized");
        }
        return gameDAO.getGame(gameId);
    }

    /** Applies a move in a game */
    public GameData makeMove(int gameId, String authToken, ChessMove move)
            throws UnauthorizedException, BadRequestException, ForbiddenException, DataAccessException, InvalidMoveException {

        var auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("unauthorized");
        }

        GameData game = gameDAO.getGame(gameId);
        if (game == null) {
            throw new BadRequestException("bad request");
        }

        ChessGame chessGame = game.chessGame();

        if (chessGame.getWinner() != null) {
            throw new ForbiddenException("game is over");
        }

        String playerColor = auth.username().equals(game.whiteUsername()) ? "WHITE" :
                auth.username().equals(game.blackUsername()) ? "BLACK" : null;

        if (playerColor == null) {
            throw new ForbiddenException("not a player");
        }
        if (!chessGame.getTeamTurn().name().equals(playerColor)){
            throw new ForbiddenException("not your turn");
        }

        chessGame.makeMove(move);

        GameData updated = game.withChessGame(chessGame);
        gameDAO.updateGame(updated);

        return updated; // IMPORTANT
    }


    /** Resign from a game */
    public String resignGame(String authToken, int gameId)
            throws UnauthorizedException, BadRequestException, ForbiddenException, DataAccessException {

        var auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("unauthorized");
        }

        GameData game = gameDAO.getGame(gameId);
        if (game == null) {
            throw new BadRequestException("bad request");
        }

        String username = auth.username();
        String white = game.whiteUsername();
        String black = game.blackUsername();

        // ❌ If not a player → observers cannot resign → throw Forbidden
        if (!username.equals(white) && !username.equals(black)) {
            throw new ForbiddenException("observer cannot resign");
        }

        // ❌ If game already has winner
        if (game.chessGame().getWinner() != null) {
            throw new ForbiddenException("game already over");
        }

        // Determine loser
        String loser = username;

        // Apply resign
        ChessGame chessGame = game.chessGame();
        chessGame.setWinner(
                username.equals(white) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE
        );

        GameData updated = game.withChessGame(chessGame);
        gameDAO.updateGame(updated);

        return loser;
    }



    /** Updates a game */
    public void updateGame(GameData game) throws DataAccessException {
        gameDAO.updateGame(game);
    }

    /** Saves a game */
    public void saveGame(GameData game) throws DataAccessException {
        gameDAO.updateGame(game);
    }

    public String leaveGame(String authToken, int gameId)
            throws UnauthorizedException, DataAccessException {

        var auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("unauthorized");
        }

        GameData game = gameDAO.getGame(gameId);
        if (game == null) {
            return null;
        }

        String username = auth.username();

        GameData updated = game;
        if (username.equals(game.whiteUsername())) {
            updated = game.withWhite(null);
        } else if (username.equals(game.blackUsername())) {
            updated = game.withBlack(null);
        }

        gameDAO.updateGame(updated);
        return username;
    }


}
