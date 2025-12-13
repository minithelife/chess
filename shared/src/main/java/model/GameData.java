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

    /** Returns the winner team color, or null if the game is not over */
    public ChessGame.TeamColor getWinner() {
        return game.getWinner();
    }

    /** Returns the winner's username, or null if the game is not over */
    public String getWinnerUsername() {
        ChessGame.TeamColor winner = getWinner();
        if (winner == null) {
            return null;
        }
        return winner == ChessGame.TeamColor.WHITE ? whiteUsername : blackUsername;
    }

    /** Returns true if the game is over (winner exists) */
    public boolean isGameOver() {
        return game.isGameOver();
    }
}
