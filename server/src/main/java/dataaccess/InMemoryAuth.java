package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.Map;

/** In-memory implementation of AuthDAO for testing */
public class InMemoryAuth implements AuthDAO {

    private final Map<String, AuthData> auths = new HashMap<>();

    @Override
    public void createAuth(AuthData auth) {
        auths.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String token) {
        return auths.get(token);
    }

    @Override
    public void deleteAuth(String token) {
        auths.remove(token);
    }

    @Override
    public void clear() {
        auths.clear();
    }
}
