package service;

import dao.*;
import handler.exceptions.UnauthorizedException;

public class Service {

    protected final AuthDAO authDAO = new AuthDAO();
    protected final GameDAO gameDAO = new GameDAO();
    protected final UserDAO userDAO = new UserDAO();

}
