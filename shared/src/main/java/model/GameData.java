package model;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    public GameData withWhite(String white) {
        return new GameData(gameID, white, blackUsername, gameName, game);
    }
    public GameData withBlack(String black) {
        return new GameData(gameID, whiteUsername, black, gameName, game);
    }
}
