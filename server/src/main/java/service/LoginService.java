package service;

import dao.AuthDAO;
import dao.UserDAO;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class LoginService {

    private final UserDAO userDAO = new UserDAO();
    private final AuthDAO authDAO = new AuthDAO();

    public AuthData login(String username, String password) throws Exception {
        if (username == null || password == null) {
            throw new Exception("bad request");
        }

        UserData user = userDAO.getUser(username);
        if (user == null || !user.password().equals(password)) {
            throw new Exception("unauthorized");
        }

        AuthData auth = new AuthData(UUID.randomUUID().toString(), username);
        authDAO.createAuth(auth);
        return auth;
    }
}
