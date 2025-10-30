package service;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import handler.exceptions.UnauthorizedException;

import static org.junit.jupiter.api.Assertions.*;

public class LogoutServiceTest {

    private LogoutService service;
    private String token;

    @BeforeEach
    public void setup() {
        service = new LogoutService();
        new ClearService().clear();
        AuthData auth = new RegisterService().register(new UserData("tommy", "pass123", "tommy@email.com"));
        token = auth.token();
    }

    @Test
    public void testLogoutSuccess() {
        service.logout(token);
        // After logout, trying to logout again should fail
        assertThrows(UnauthorizedException.class, () -> service.logout(token));
    }

    @Test
    public void testLogoutInvalidToken() {
        assertThrows(UnauthorizedException.class, () -> service.logout("invalidtoken"));
    }
}
