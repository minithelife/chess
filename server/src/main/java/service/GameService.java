package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.GameData;
import model.UserData;
import requests.CreateGameResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles chess game operations: create, join, and list games.
 */
public class GameService {
    private final DataAccess dao;

    public GameService(DataAccess dao) {
        this.dao = dao;
    }

    /** Creates a new game and returns its ID. */
    /** Create a new createGame file with setters and getters. make this in shared/.../requests and results/java . also use records**/
    public CreateGameResult createGame(String name, String username) throws DataAccessException {
        if (name == null || name.isBlank()) throw new BadRequestException("Game name cannot be null or empty");

        int nextGameId = dao.getNextGameId();
        GameData newGame = new GameData(nextGameId, name, null, null);
        dao.createGame(newGame);

        return new CreateGameResult(nextGameId);
    }

    /**
     * Attempts to join a game as the given color.
     */
    public void joinGame(int gameId, String username, String color) throws DataAccessException {
        GameData game = dao.getGame(gameId);
        if (game == null) throw new BadRequestException("Game not found");
        if (username == null || username.isBlank()) throw new BadRequestException("Username cannot be null or empty");

        GameData updatedGame;

        switch (color.toUpperCase()) {
            case "WHITE" -> {
                if (game.white() != null) {
                    throw new ForbiddenException("White already taken");
                }
                updatedGame = game.withWhite(username);
            }
            case "BLACK" -> {
                if (game.black() != null) {
                    throw new ForbiddenException("Black already taken");
                }
                updatedGame = game.withBlack(username);
            }
            default -> throw new BadRequestException("Invalid team color: " + color);
        }

        dao.updateGame(updatedGame);
    }


    /** Lists all games with their current player assignments. */
    public List<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(dao.getAllGames());
    }

    // Custom exceptions
    public static class BadRequestException extends DataAccessException {
        public BadRequestException(String msg) { super(msg); }
    }

    public static class ForbiddenException extends DataAccessException {
        public ForbiddenException(String msg) { super(msg); }
    }
}
