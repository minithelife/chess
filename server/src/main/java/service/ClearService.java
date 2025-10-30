package service;
import dao.*;

public class ClearService {
    public void clear() throws Exception{
        System.out.println("works");
        UserDAO userDAO = new UserDAO();
        userDAO.clearDAO();
        //throw new Exception("does not work");

    }

}
