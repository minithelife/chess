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
    void prepareDatabase() throws DataAccessException {
        userDAO.clear();
        // Create testUser for retrieval tests
        userDAO.createUser(new UserData("testUser", "pass123", "test@example.com"));
    }

    @Test
    @Order(1)
    @DisplayName("Positive: create user successfully")
    void testCreateUserSuccess() throws DataAccessException {
        // This user is already created in prepareDatabase,
        // so for uniqueness test create a different user here
        userDAO.createUser(new UserData("newUser", "newPass", "new@example.com"));
        // No exception means success
    }

    @Test
    @Order(2)
    @DisplayName("Positive: get existing user successfully")
    void testGetUserSuccess() throws DataAccessException {
        UserData fetched = userDAO.getUser("testUser");
        assertNotNull(fetched);
        assertEquals("testUser", fetched.username());
        assertEquals("pass123", fetched.password());
        assertEquals("test@example.com", fetched.email());
    }

    @Test
    @Order(3)
    @DisplayName("Negative: create user with duplicate username throws exception")
    void testCreateUserDuplicateThrows() throws DataAccessException {
        UserData duplicate = new UserData("testUser", "pass2", "dup@example.com");
        DataAccessException ex = assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(duplicate);
        });
        assertTrue(ex.getMessage().toLowerCase().contains("failed to create user"));
    }

    @Test
    @Order(4)
    @DisplayName("Negative: get user that doesn't exist returns null")
    void testGetUserNotFound() throws DataAccessException {
        UserData fetched = userDAO.getUser("nonexistent");
        assertNull(fetched);
    }

    @Test
    @Order(5)
    @DisplayName("Positive: clear users removes all")
    void testClearUsers() throws DataAccessException {
        userDAO.clear();

        assertNull(userDAO.getUser("testUser"));
    }
}
