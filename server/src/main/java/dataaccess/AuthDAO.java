package dataaccess;

import model.AuthData;

public interface AuthDAO {

    void createAuth(AuthData auth);
    AuthData getAuth(String token);
    void deleteAuth(String token);

    void clear();
}
