package dataaccess;

import model.AuthData;
import model.UserData;
import model.GameData;

import java.util.*;

/** In-memory implementation for testing */
public class InMemoryDataAccess implements DataAccess {

    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> auths = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameId = 1;

    @Override
    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void createAuth(AuthData auth) {
        auths.put(auth.token(), auth);
    }

    @Override
    public AuthData getAuth(String token) {
        return auths.get(token);
    }

    @Override
    public void deleteAuth(String token) {
        auths.remove(token);
    }

    @Override
    public void createGame(GameData game) {
        games.put(game.gameId(), game);
    }

    @Override
    public GameData getGame(int gameId) {
        return games.get(gameId);
    }

    @Override
    public void updateGame(GameData game) {
        games.put(game.gameId(), game);
    }

    @Override
    public List<GameData> getAllGames() {
        return new ArrayList<>(games.values());
    }

    @Override
    public int getNextGameId() {
        return nextGameId++;
    }

    @Override
    public void clear() {
        users.clear();
        auths.clear();
        games.clear();
        nextGameId = 1;
    }
}
