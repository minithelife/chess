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
    /** Returns a new GameData with updated game state */
    public GameData withGame(ChessGame newGame) {
        return new GameData(gameID, gameName, whiteUsername, blackUsername, newGame);
    }

    public ChessGame chessGame() {
        return game;
    }

    public GameData withChessGame(ChessGame chessGame) {
        return new GameData(gameID, gameName, whiteUsername, blackUsername, chessGame);
    }

    public Object getChessGame() {
        return game;
    }

    public boolean isCheckmate() {
        if (game == null) return false;

        // Check if either team is in checkmate
        return game.isInCheckmate(ChessGame.TeamColor.WHITE) ||
                game.isInCheckmate(ChessGame.TeamColor.BLACK);
    }

    /** Optional: returns the username of the winner if checkmate, null otherwise */
    public String getWinner() {
        if (game == null) return null;

        ChessGame.TeamColor winnerTeam = game.getWinner();
        if (winnerTeam == null) {
            // if winner is not set, try to determine from checkmate state
            if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) return blackUsername;
            if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) return whiteUsername;
            return null;
        }

        return (winnerTeam == ChessGame.TeamColor.WHITE) ? whiteUsername : blackUsername;
    }

}
