package service;

import dao.UserDAO;

public class ClearService extends Service {

    public void clear() {
//        UserDAO userDAO = new UserDAO();
        userDAO.clearDAO();
    }
}
