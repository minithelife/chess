package service;

import dataaccess.GameDAO;
import dataaccess.AuthDAO;
import dataaccess.UserDAO;

public class ClearService {

    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final UserDAO userDAO;

    // Constructor saves the DAOs to fields
    public ClearService(AuthDAO authDAO, GameDAO gameDAO, UserDAO userDAO){
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.userDAO = userDAO;
    }

    public void clear() {
        userDAO.clear();
        gameDAO.clear();
        authDAO.clear();
    }
}