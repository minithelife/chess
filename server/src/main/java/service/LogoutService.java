package service;

import dao.AuthDAO

public class LogoutService {
    private final AuthDAO authDAO = new AuthDAO();

    public void logout(String token) throws Exception{
        if (token == null || authDAO.getAuth(token) == null) {
            throw new Exception("unauthorized");
        }
        authDAO.clear(); // OR just delete the single token
        authDAO.deleteAuth(token);
    }
}
