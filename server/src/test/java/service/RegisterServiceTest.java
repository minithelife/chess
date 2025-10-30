package service;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import handler.exceptions.BadRequestException;
import handler.exceptions.ForbiddenException;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterServiceTest {

    private RegisterService service;

    @BeforeEach
    public void setup() {
        service = new RegisterService();
        new ClearService().clear(); // make sure DAO is empty
    }

    @Test
    public void testRegisterSuccess() {
        UserData user = new UserData("tommy", "pass123", "tommy@email.com");
        AuthData auth = service.register(user);

        assertNotNull(auth);
        assertEquals("tommy", auth.username());
        assertNotNull(auth.token());
    }

    @Test
    public void testRegisterMissingField() {
        UserData user = new UserData("tommy", null, "tommy@email.com");
        assertThrows(BadRequestException.class, () -> service.register(user));
    }

    @Test
    public void testRegisterDuplicateUsername() {
        UserData user = new UserData("tommy", "pass123", "tommy@email.com");
        service.register(user);

        UserData duplicate = new UserData("tommy", "pass456", "other@email.com");
        assertThrows(ForbiddenException.class, () -> service.register(duplicate));
    }
}
