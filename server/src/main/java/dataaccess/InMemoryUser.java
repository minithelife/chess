package dataaccess;

import model.UserData;
import java.util.HashMap;
import java.util.Map;

/** In-memory implementation of UserDAO for testing */
public class InMemoryUser implements UserDAO {

    private final Map<String, UserData> users = new HashMap<>();

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
    public void clear() {
        users.clear();
    }
}
