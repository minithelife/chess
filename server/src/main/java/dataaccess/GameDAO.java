package dataaccess;

import model.GameData;
import java.util.List;

public interface GameDAO {

    int createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameId) throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
    List<GameData> getAllGames() throws DataAccessException;

    void clear() throws DataAccessException;
}
