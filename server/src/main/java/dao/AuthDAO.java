package dao;

import model.AuthData;
import java.util.HashMap;
import java.util.Map;

public class AuthDAO {
    private static final Map<String, AuthData> auths = new HashMap<>();

    public void createAuth(AuthData auth) {
        auths.put(auth.token(), auth);
    }

    public AuthData getAuth(String token) {
        return auths.get(token);
    }

    public void clear() {
        auths.clear();
    }
}