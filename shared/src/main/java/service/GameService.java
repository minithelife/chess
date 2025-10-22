package service;

import dataaccess.*;
import model.*;
import chess.ChessGame;

import java.util.*;
import java.util.stream.Collectors;

public class GameService {
    private final DataAccess dao;
    public GameService(DataAccess dao) { this.dao = dao; }

    public CreateGameResult createGame(CreateGameRequest req, String username) throws DataAccessException {
        if (req == null || req.gameName == null) throw new UserService.BadRequestException("bad request");
        if (username == null) throw new UserService.UnauthorizedException("unauthorized");

        // initial ChessGame can be default-constructed
        ChessGame cg = new ChessGame();
        GameData g = new GameData(0, null, null, req.gameName, cg);
        int id = dao.createGame(g);
        return new CreateGameResult(id);
    }

    public void joinGame(JoinGameRequest req, String username) throws DataAccessException {
        if (req == null || req.playerColor == null || req.gameID == null) throw new UserService.BadRequestException("bad request");
        if (username == null) throw new UserService.UnauthorizedException("unauthorized");

        GameData g = dao.getGame(req.gameID);
        if (g == null) throw new UserService.BadRequestException("bad request");

        String color = req.playerColor;
        if ("WHITE".equalsIgnoreCase(color)) {
            if (g.whiteUsername() != null) throw new UserService.ForbiddenException("already taken");
            g = g.withWhite(username);
        } else if ("BLACK".equalsIgnoreCase(color)) {
            if (g.blackUsername() != null) throw new UserService.ForbiddenException("already taken");
            g = g.withBlack(username);
        } else {
            throw new UserService.BadRequestException("bad request");
        }

        dao.updateGame(g);
    }

    public List<ListEntry> listGames() throws DataAccessException {
        List<GameData> g = dao.listGames();
        return g.stream().map(it -> new ListEntry(it.gameID(), it.whiteUsername(), it.blackUsername(), it.gameName()))
                .collect(Collectors.toList());
    }

    // helper DTOs
    public static record ListEntry(int gameID, String whiteUsername, String blackUsername, String gameName) {}
}
