package model;

/** Represents a chess game with player assignments */
public record GameData(int gameId, String name, String white, String black) {

    public GameData withWhite(String username) {
        return new GameData(gameId, name, username, black);
    }

    public GameData withBlack(String username) {
        return new GameData(gameId, name, white, username);
    }
}
