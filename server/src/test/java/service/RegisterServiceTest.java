package service;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import handler.exceptions.BadRequestException;
import handler.exceptions.ForbiddenException;
import dataaccess.*;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterServiceTest {

    private RegisterService service;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    @BeforeEach
    public void setup() {
        authDAO = new InMemoryAuth();
        userDAO = new InMemoryUser();
        new ClearService(authDAO, new InMemoryGame(), userDAO).clear();

        service = new RegisterService(userDAO, authDAO);
    }

    @Test
    public void testRegisterSuccess() throws ForbiddenException, BadRequestException {
        UserData user = new UserData("tommy", "pass123", "tommy@email.com");
        AuthData auth = service.register(user);

        assertNotNull(auth);
        assertEquals("tommy", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    public void testRegisterMissingField() {
        UserData user = new UserData("tommy", null, "tommy@email.com");
        assertThrows(BadRequestException.class, () -> service.register(user));
    }

    @Test
    public void testRegisterDuplicateUsername() throws ForbiddenException, BadRequestException {
        UserData user = new UserData("tommy", "pass123", "tommy@email.com");
        service.register(user);

        UserData duplicate = new UserData("tommy", "pass456", "other@email.com");
        assertThrows(ForbiddenException.class, () -> service.register(duplicate));
    }
}
