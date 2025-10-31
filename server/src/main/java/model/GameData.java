package model;

import chess.ChessGame;

/** Represents a chess game with player assignments */
public record GameData(
        int gameID,
        String gameName,
        String whiteUsername,
        String blackUsername,
        ChessGame game
) {

    /** Returns a new GameData with white player set */
    public GameData withWhite(String username) {
        return new GameData(gameID, gameName, username, blackUsername, game);
    }

    /** Returns a new GameData with black player set */
    public GameData withBlack(String username) {
        return new GameData(gameID, gameName, whiteUsername, username, game);
    }
}
