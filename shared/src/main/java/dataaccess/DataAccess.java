package dataaccess;

import model.*;

import java.util.List;

public interface DataAccess {
    void clear() throws DataAccessException;

    // User
    void createUser(UserData u) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    boolean userExists(String username) throws DataAccessException;

    // Auth
    void createAuth(AuthData a) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;

    // Game
    int createGame(GameData g) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
    void updateGame(GameData g) throws DataAccessException;
}
