package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import handler.exceptions.BadRequestException;
import handler.exceptions.ForbiddenException;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class RegisterService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public RegisterService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register(UserData user) throws ForbiddenException, BadRequestException, DataAccessException {
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new BadRequestException("bad request");
        }

        if (userDAO.getUser(user.username()) != null) {
            throw new ForbiddenException("already taken");
        }

        //hash password
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        UserData hashedUser = new UserData(user.username(), hashedPassword, user.email());
        userDAO.createUser(hashedUser);

        AuthData auth = new AuthData(UUID.randomUUID().toString(), user.username());
        authDAO.createAuth(auth);
        return auth;
    }
}
