package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import handler.exceptions.BadRequestException;
import handler.exceptions.UnauthorizedException;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class LoginService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public LoginService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData login(String username, String password) throws BadRequestException, UnauthorizedException {
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
