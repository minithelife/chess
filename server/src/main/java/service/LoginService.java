package service;

import dao.AuthDAO;
import dao.UserDAO;
import handler.exceptions.BadRequestException;
import handler.exceptions.UnauthorizedException;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class LoginService extends Service {

//    private final UserDAO userDAO = new UserDAO();
//    private final AuthDAO authDAO = new AuthDAO();

    public AuthData login(String username, String password) {
        if (username == null || password == null) {
            throw new BadRequestException("bad request");
        }

        UserData user = userDAO.getUser(username);
        if (user == null || !user.password().equals(password)) {
            throw new UnauthorizedException("unauthorized");
        }

        AuthData auth = new AuthData(UUID.randomUUID().toString(), username);
        authDAO.createAuth(auth);
        return auth;
    }
}
