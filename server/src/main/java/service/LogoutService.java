package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import handler.exceptions.UnauthorizedException;

public class LogoutService {

    private final AuthDAO authDAO;

    public LogoutService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    public void logout(String token) throws UnauthorizedException, DataAccessException {
        if (token == null || authDAO.getAuth(token) == null) {
            throw new UnauthorizedException("unauthorized");
        }
        authDAO.deleteAuth(token);
    }
}
