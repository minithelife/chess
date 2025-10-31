package dataaccess;

import model.UserData;

public interface UserDAO {
    void createUser(UserData user);
    UserData getUser(String username);
    void clear();
    boolean userExists(String username); // add this
}
