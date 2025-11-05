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
        userDAO = new MySqlUserDAO();

        userDAO.clear();
        userDAO.createUser(new UserData("user1", "password", "user1@example.com"));
        userDAO.createUser(new UserData("userDel", "password", "userdel@example.com"));
        userDAO.createUser(new UserData("userClear", "password", "userclear@example.com"));
    }

    @BeforeEach
    void clearAuth() throws DataAccessException {
        authDAO.clear();
    }

    // Helper methods
    private void createAuthToken(String token, String username) throws DataAccessException {
        authDAO.createAuth(new AuthData(token, username));
    }

    private void assertAuthDataEquals(AuthData authData, String expectedToken, String expectedUser) {
        assertNotNull(authData);
        assertEquals(expectedToken, authData.authToken());
        assertEquals(expectedUser, authData.username());
    }

    @Test
    @Order(1)
    @DisplayName("Positive: create and get auth token")
    void testCreateAndGetAuthSuccess() throws DataAccessException {
        createAuthToken("token123", "user1");
        AuthData fetched = authDAO.getAuth("token123");
        assertAuthDataEquals(fetched, "token123", "user1");
    }

    @Test
    @Order(2)
    @DisplayName("Negative: create auth with duplicate token throws exception")
    void testCreateAuthDuplicateTokenThrows() throws DataAccessException {
        createAuthToken("tokenDup", "user1");
        DataAccessException ex = assertThrows(DataAccessException.class, () -> {
            createAuthToken("tokenDup", "user1");
        });
        assertTrue(ex.getMessage().toLowerCase().contains("failed to create auth"));
    }

    @Test
    @Order(3)
    @DisplayName("Positive: get auth token returns valid AuthData")
    void testGetAuthSuccess() throws DataAccessException {
        createAuthToken("token123", "user1");
        AuthData fetched = authDAO.getAuth("token123");
        assertAuthDataEquals(fetched, "token123", "user1");
    }

    @Test
    @Order(4)
    @DisplayName("Negative: get auth token that doesn't exist returns null")
    void testGetAuthNotFound() throws DataAccessException {
        AuthData fetched = authDAO.getAuth("nonexistent");
        assertNull(fetched);
    }

    @Test
    @Order(5)
    @DisplayName("Positive: delete auth token")
    void testDeleteAuthSuccess() throws DataAccessException {
        createAuthToken("tokenToDelete", "userDel");
        authDAO.deleteAuth("tokenToDelete");
        AuthData fetched = authDAO.getAuth("tokenToDelete");
        assertNull(fetched);
    }

    @Test
    @Order(6)
    @DisplayName("Negative: delete auth token that doesn't exist does not throw")
    void testDeleteAuthNonExistent() {
        assertDoesNotThrow(() -> authDAO.deleteAuth("nonexistentToken"));
    }

    @Test
    @Order(7)
    @DisplayName("Positive: clear auth tokens")
    void testClearAuth() throws DataAccessException {
        createAuthToken("tokenClear", "userClear");
        authDAO.clear();
        AuthData fetched = authDAO.getAuth("tokenClear");
        assertNull(fetched);
    }
}
