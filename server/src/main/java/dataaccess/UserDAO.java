package dataaccess;

import model.UserData;

public interface UserDAO {

    boolean userExists(String username);
    void createUser(UserData user);
    UserData getUser(String username);

    void clear();
}