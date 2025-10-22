package dataaccess;

import model.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryDataAccess implements DataAccess {
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> auths = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private final AtomicInteger nextGameId = new AtomicInteger(1);

    @Override
    public synchronized void clear() {
        users.clear();
        auths.clear();
        games.clear();
        nextGameId.set(1);
    }

    @Override
    public synchronized void createUser(UserData u) throws DataAccessException {
        if (u == null || u.username() == null) throw new DataAccessException("Invalid user");
        if (users.containsKey(u.username())) throw new DataAccessException("User already exists");
        users.put(u.username(), u);
    }

    @Override
    public synchronized UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    @Override
    public synchronized boolean userExists(String username) throws DataAccessException {
        return users.containsKey(username);
    }

    @Override
    public synchronized void createAuth(AuthData a) throws DataAccessException {
        auths.put(a.authToken(), a);
    }

    @Override
    public synchronized AuthData getAuth(String authToken) throws DataAccessException {
        return auths.get(authToken);
    }

    @Override
    public synchronized void deleteAuth(String authToken) throws DataAccessException {
        auths.remove(authToken);
    }

    @Override
    public synchronized int createGame(GameData g) throws DataAccessException {
        int id = nextGameId.getAndIncrement();
        GameData store = new GameData(id, g.whiteUsername(), g.blackUsername(), g.gameName(), g.game());
        games.put(id, store);
        return id;
    }

    @Override
    public synchronized GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }

    @Override
    public synchronized List<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(games.values());
    }

    @Override
    public synchronized void updateGame(GameData g) throws DataAccessException {
        if (!games.containsKey(g.gameID())) throw new DataAccessException("Game not found");
        games.put(g.gameID(), g);
    }
}
