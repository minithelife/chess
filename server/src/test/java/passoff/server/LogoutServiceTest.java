package passoff.server;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import handler.exceptions.UnauthorizedException;
import dataaccess.*;

import static org.junit.jupiter.api.Assertions.*;

public class LogoutServiceTest {

    private LogoutService service;
    private AuthDAO authDAO;
    private UserDAO userDAO;
    private String token;

    @BeforeEach
    public void setup() {
        authDAO = new InMemoryAuth();
        userDAO = new InMemoryUser();
        new ClearService(authDAO, new InMemoryGame(), userDAO).clear();

        service = new LogoutService(authDAO);

        token = new RegisterService(userDAO, authDAO)
                .register(new UserData("tommy", "pass123", "tommy@email.com"))
                .authToken();
    }

    @Test
    public void testLogoutSuccess() {
        service.logout(token);
        assertThrows(UnauthorizedException.class, () -> service.logout(token));
    }

    @Test
    public void testLogoutInvalidToken() {
        assertThrows(UnauthorizedException.class, () -> service.logout("invalidtoken"));
    }
}
