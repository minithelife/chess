package dao;

import model.UserData;

public class UserDAO extends DAO {

//    private final InMemoryDataAccess dataAccess = new InMemoryDataAccess();

    public UserData getUser(String username) {
        return dataAccess.getUser(username);
    }

    public void createUser(UserData user) {
        dataAccess.createUser(user);
    }

    public void clearDAO() {
        dataAccess.clear();
    }
}
