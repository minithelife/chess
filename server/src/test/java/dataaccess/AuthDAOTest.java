package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthDAOTest {

    private static AuthDAO authDAO;
    private static UserDAO userDAO;

    @BeforeAll
    static void setup() throws DataAccessException {
        authDAO = new MySqlAuthDAO();
        userDAO = new MySqlUserDAO();  // initialize user DAO

        // Clear and create test users needed for auth tokens
        userDAO.clear();
        userDAO.createUser(new UserData("user1", "password", "user1@example.com"));
        userDAO.createUser(new UserData("userDel", "password", "userdel@example.com"));
        userDAO.createUser(new UserData("userClear", "password", "userclear@example.com"));
    }

    @BeforeEach
    void clearAuth() throws DataAccessException {
        authDAO.clear();
    }

    @Test
    @Order(1)
    @DisplayName("Positive: create and get auth token")
    void testCreateAndGetAuthSuccess() throws DataAccessException {
        AuthData auth = new AuthData("token123", "user1");
        authDAO.createAuth(auth);

        AuthData fetched = authDAO.getAuth("token123");
        assertNotNull(fetched);
        assertEquals("token123", fetched.authToken());
        assertEquals("user1", fetched.username());
    }

    @Test
    @Order(2)
    @DisplayName("Negative: get auth token that doesn't exist returns null")
    void testGetAuthNotFound() throws DataAccessException {
        AuthData fetched = authDAO.getAuth("nonexistent");
        assertNull(fetched);
    }

    @Test
    @Order(3)
    @DisplayName("Positive: delete auth token")
    void testDeleteAuthSuccess() throws DataAccessException {
        AuthData auth = new AuthData("tokenToDelete", "userDel");
        authDAO.createAuth(auth);

        authDAO.deleteAuth("tokenToDelete");
        AuthData fetched = authDAO.getAuth("tokenToDelete");
        assertNull(fetched);
    }

    @Test
    @Order(4)
    @DisplayName("Negative: delete auth token that doesn't exist does not throw")
    void testDeleteAuthNonExistent() {
        assertDoesNotThrow(() -> authDAO.deleteAuth("nonexistentToken"));
    }

    @Test
    @Order(5)
    @DisplayName("Positive: clear auth tokens")
    void testClearAuth() throws DataAccessException {
        AuthData auth = new AuthData("tokenClear", "userClear");
        authDAO.createAuth(auth);

        authDAO.clear();

        AuthData fetched = authDAO.getAuth("tokenClear");
        assertNull(fetched);
    }
}
