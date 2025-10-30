package dao;

import dataaccess.InMemoryDataAccess;
import model.GameData;

import java.util.List;

public class GameDAO extends DAO {

//    private final InMemoryDataAccess dataAccess = new InMemoryDataAccess();

    public void createGame(GameData game) {
        dataAccess.createGame(game);
    }

    public GameData getGame(int gameId) {
        return dataAccess.getGame(gameId);
    }

    public List<GameData> getAllGames() {
        return dataAccess.getAllGames();
    }

    public void updateGame(GameData game) {
        dataAccess.updateGame(game);
    }

    public int getNextGameId() {
        return dataAccess.getNextGameId();
    }

    public void clearGames() {
        dataAccess.clear();
    }
}
