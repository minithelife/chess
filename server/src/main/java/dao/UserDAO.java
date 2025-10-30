package dao;

import dataaccess.*;

import model.UserData;
import model.AuthData;
import java.util.HashMap;
import java.util.Map;

public class UserDAO {
    private final InMemoryDataAccess dataAccess = new InMemoryDataAccess();
    public void clearDAO(){
        dataAccess.clear();
    }

    public UserData getUser(String username) {
        return dataAccess.getUser(username);
    }

    public void createUser(UserData user) {
        dataAccess.createUser(user);
    }

}
