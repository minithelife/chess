package service;

import dao.AuthDAO;
import handler.exceptions.UnauthorizedException;

public class LogoutService extends Service {

//    private final AuthDAO authDAO = new AuthDAO();

    public void logout(String token) {
        if (token == null || authDAO.getAuth(token) == null) {
            throw new UnauthorizedException("unauthorized");
        }
        authDAO.deleteAuth(token); // remove only the specific token
    }
}
