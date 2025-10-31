package dataaccess;

import model.GameData;
import java.util.List;

public interface GameDAO {

    void createGame(GameData game);
    GameData getGame(int gameId);
    void updateGame(GameData game);
    List<GameData> getAllGames();

    int getNextGameId();
    void clear();
}
