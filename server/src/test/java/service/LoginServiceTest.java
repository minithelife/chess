package service;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import handler.exceptions.BadRequestException;
import handler.exceptions.UnauthorizedException;
import dataaccess.*;

import static org.junit.jupiter.api.Assertions.*;

public class LoginServiceTest {

    private LoginService service;
    private AuthDAO authDAO;
    private UserDAO userDAO;

    @BeforeEach
    public void setup() {
        authDAO = new InMemoryAuth();
        userDAO = new InMemoryUser();
        new ClearService(authDAO, new InMemoryGame(), userDAO).clear();

        service = new LoginService(userDAO, authDAO);
        new RegisterService(userDAO, authDAO)
                .register(new UserData("tommy", "pass123", "tommy@email.com"));
    }

    @Test
    public void testLoginSuccess() {
        AuthData auth = service.login("tommy", "pass123");
        assertNotNull(auth);
        assertEquals("tommy", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    public void testLoginWrongPassword() {
        assertThrows(UnauthorizedException.class, () -> service.login("tommy", "wrongpass"));
    }

    @Test
    public void testLoginNonexistentUser() {
        assertThrows(UnauthorizedException.class, () -> service.login("unknown", "pass"));
    }

    @Test
    public void testLoginMissingFields() {
        assertThrows(BadRequestException.class, () -> service.login(null, "pass123"));
        assertThrows(BadRequestException.class, () -> service.login("tommy", null));
    }
}
