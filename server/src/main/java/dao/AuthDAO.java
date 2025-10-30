package dao;

import dataaccess.InMemoryDataAccess;
import model.AuthData;

public class AuthDAO extends DAO {

//    private final InMemoryDataAccess dataAccess = new InMemoryDataAccess();

    public void createAuth(AuthData auth) {
        dataAccess.createAuth(auth);
    }

    public AuthData getAuth(String token) {
        return dataAccess.getAuth(token);
    }

    public void deleteAuth(String token) {
        dataAccess.deleteAuth(token);
    }

    public void clear() {
        dataAccess.clear(); // optional: clears everything
    }
}
