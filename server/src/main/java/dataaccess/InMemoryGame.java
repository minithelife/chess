package dataaccess;

import model.GameData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** In-memory implementation of GameDAO for testing */
public class InMemoryGame implements GameDAO {

    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameId = 1;

    @Override
    public int createGame(GameData game) {
        int id = nextGameId++;

        GameData stored = new GameData(
                id,
                game.gameName(),
                game.whiteUsername(),
                game.blackUsername(),
                game.chessGame()
        );

        games.put(id, stored);
        return id;
    }


    @Override
    public GameData getGame(int gameId) {
        return games.get(gameId);
    }

    @Override
    public void updateGame(GameData game) {
        games.put(game.gameID(), game);
    }

    @Override
    public List<GameData> getAllGames() {
        return new ArrayList<>(games.values());
    }

    @Override
    public void clear() {
        games.clear();
        nextGameId = 1;
    }
}
