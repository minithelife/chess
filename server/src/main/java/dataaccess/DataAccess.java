package dataaccess;

import model.AuthData;
import model.UserData;
import model.GameData;
import java.util.List;

public interface DataAccess {

    // User operations
    boolean userExists(String username) throws DataAccessException;
    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;

    // Auth operations
    void createAuth(AuthData auth) throws DataAccessException;
    AuthData getAuth(String token) throws DataAccessException;
    void deleteAuth(String token) throws DataAccessException;

    // Game operations
    void createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameId) throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
    List<GameData> getAllGames() throws DataAccessException;

    // Misc
    int getNextGameId() throws DataAccessException;
    void clear() throws DataAccessException;
}
