package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDAOTest {

    private static UserDAO userDAO;

    @BeforeAll
    static void setup() {
        userDAO = new MySqlUserDAO();
    }

    @BeforeEach
    void clearUsers() throws DataAccessException {
        userDAO.clear();  // clean before each test to avoid data collision
    }

    @Test
    @Order(1)
    @DisplayName("Positive: create and get user")
    void testCreateAndGetUserSuccess() throws DataAccessException {
        UserData user = new UserData("testUser", "pass123", "test@example.com");
        userDAO.createUser(user);

        UserData fetched = userDAO.getUser("testUser");
        assertNotNull(fetched);
        assertEquals("testUser", fetched.username());
        assertEquals("pass123", fetched.password());
        assertEquals("test@example.com", fetched.email());
    }

    @Test
    @Order(2)
    @DisplayName("Negative: get user that doesn't exist returns null")
    void testGetUserNotFound() throws DataAccessException {
        UserData fetched = userDAO.getUser("nonexistent");
        assertNull(fetched);
    }

    @Test
    @Order(3)
    @DisplayName("Negative: create user with duplicate username throws exception")
    void testCreateUserDuplicateThrows() throws DataAccessException {
        UserData user = new UserData("dupUser", "pass", "dup@example.com");
        userDAO.createUser(user);

        UserData duplicate = new UserData("dupUser", "pass2", "dup2@example.com");
        DataAccessException ex = assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(duplicate);
        });
        assertTrue(ex.getMessage().contains("Failed to create user"));
    }

    @Test
    @Order(4)
    @DisplayName("Positive: clear users removes all")
    void testClearUsers() throws DataAccessException {
        UserData user = new UserData("user1", "pass", "email@example.com");
        userDAO.createUser(user);

        userDAO.clear();

        assertNull(userDAO.getUser("user1"));
    }
}
