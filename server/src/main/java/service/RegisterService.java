package service;

import dao.AuthDAO;
import dao.UserDAO;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class RegisterService {

    private final UserDAO userDAO = new UserDAO();
    private final AuthDAO authDAO = new AuthDAO();

    public AuthData register(UserData user) throws Exception {
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new Exception("bad request");
        }

        if (userDAO.getUser(user.username()) != null) {
            throw new Exception("already taken");
        }

        userDAO.createUser(user);
        AuthData auth = new AuthData(UUID.randomUUID().toString(), user.username());
        authDAO.createAuth(auth);

        return auth;
    }
}
