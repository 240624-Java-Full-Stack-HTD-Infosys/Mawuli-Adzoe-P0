package net.revature.labs.dao;
import net.revature.labs.dao.util.DBUtil;
import net.revature.labs.model.User;
import util.TestUtil;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains unit tests for the UserDAOImpl class.
 * It tests various methods of the UserDAOImpl class such as addUser, updateUser, deleteUser, registerUser, and loginUser.
 * Each test case verifies the expected behavior of the corresponding method.
 */
class UserDAOTest{
    private static UserDAOImpl userDAO;
    @BeforeAll
    public static void setup() throws SQLException, IOException, ClassNotFoundException {
        TestUtil.setEnvironmentToTest();
        userDAO = new UserDAOImpl();
        DBUtil.resetTestDatabase();
    }

    @AfterAll
    public static void teardown() throws ClassNotFoundException, IOException, SQLException {
        DBUtil.resetTestDatabase();
        userDAO = null;
    }

@Test
    void testRegisterUser() throws SQLException, ClassNotFoundException, IOException {
        //Arrange
        User user = new User("John Doe", "john@example2.com", "1101234567", "password", false);

        //Act
        userDAO.registerUser(user);
        User registeredUser = userDAO.loginUser(user.getEmail(), user.getPassword());

        //Assert
        assertNotNull(registeredUser);
        assertEquals(user.getName(), registeredUser.getName());
        assertEquals(user.getEmail(), registeredUser.getEmail());
        assertEquals(user.getPhone(), registeredUser.getPhone());
        assertEquals(user.getPassword(), registeredUser.getPassword());
        assertEquals(user.isAdmin(), registeredUser.isAdmin());
    }

 @Test
    void testLoginUser() throws SQLException, ClassNotFoundException, IOException {
        //Arrange
        User user = new User("John Doe", "john5@example.com", "8101234567", "password", false);

        //Act
        userDAO.registerUser(user);
        User loggedInUser = userDAO.loginUser(user.getEmail(), user.getPassword());

        //Assert
        assertNotNull(loggedInUser);
        assertEquals(user.getName(), loggedInUser.getName());
        assertEquals(user.getEmail(), loggedInUser.getEmail());
        assertEquals(user.getPhone(), loggedInUser.getPhone());
        assertEquals(user.getPassword(), loggedInUser.getPassword());
        assertEquals(user.isAdmin(), loggedInUser.isAdmin());
    }

    @Test
    void testAddUser() throws SQLException, ClassNotFoundException, IOException {
        //Arrange
        User newUser = new User("Alice Johnson", "alice@example.com", "6783456123", "password", false);

        //Act
        userDAO.registerUser(newUser);
        User retrievedUser = userDAO.getUserByEmail(newUser.getEmail());
        
        //Assert
        assertNotNull(retrievedUser);
        assertEquals(newUser.getName(), retrievedUser.getName());
        assertEquals(newUser.getEmail(), retrievedUser.getEmail());
        assertEquals(newUser.getPassword(), retrievedUser.getPassword());
    }

    @Test
    void testUpdateUser() throws SQLException, ClassNotFoundException, IOException {
        //Arrange
        String oldEmail = "oldEmail@example.com";
        userDAO.registerUser(new User("John Doe", oldEmail, "7101234567", "password", false));
        User newUser = userDAO.getUserByEmail(oldEmail);
        String updatedEmail = "newEmail@example.com";
        newUser.setEmail(updatedEmail);

        //Act
        userDAO.updateUser(newUser);
        User updatedUser = userDAO.getUserByEmail(updatedEmail);

        //Assert
        assertNotNull(updatedUser);
        assertEquals(newUser.getEmail(), updatedUser.getEmail());
        assertNotEquals(oldEmail, updatedUser.getEmail());
        assertEquals(newUser.getName(), updatedUser.getName());
        assertEquals(newUser.getPhone(), updatedUser.getPhone());
        assertEquals(newUser.getPassword(), updatedUser.getPassword());
        assertEquals(newUser.isAdmin(), updatedUser.isAdmin());
    }

    @Test
    void testDeleteUser() throws SQLException, ClassNotFoundException, IOException {
        //Arrange
        String userEmail = "jane@example.com";
        userDAO.registerUser(new User("Jon Doe", userEmail, "4385438475", "passwoord", false));
        User foundUser = userDAO.getUserByEmail(userEmail);

        //Act
        userDAO.deleteUserByEmail(userEmail);
        User deletedUser = userDAO.getUserByEmail(userEmail);

        //Assert
        assertNull(deletedUser);
        assertEquals(userEmail, foundUser.getEmail());
        assertNotEquals(foundUser, deletedUser);
    }

    @Test
    void testExceptionHandling() throws SQLException, ClassNotFoundException, IOException {
        // Test exception scenarios, if applicable
        //Arrange
        User originalUser = new User("John Doe", "johndoe@gmail.com", "5101234567", "password", false);
        User duplicateUser = new User("Mr Beenie", "johndoe@gmail.com", "6101234567", "password", false);

        //Act
        //do first registration. That shouwld work.
        userDAO.registerUser(originalUser);
        
        //Assert
        // Attempt to register a user with duplicate email (email is unique)
        assertThrows(SQLException.class, () -> userDAO.registerUser(duplicateUser));
    }

    
   
    @Test
    void testInvalidUserRegistration() throws SQLException {
        //Arrange
        String name, invalidEmail, password, phone;
        phone = "7101234567";
        boolean isAdmin = false;
        invalidEmail = "john@.com";
        name = "John Doe";
        password = "password";

        //Act and Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userDAO.registerUser(new User(name, invalidEmail, phone, password, isAdmin));
        });
    }

    @Test
    void testInvalidUserCreation() {
        //Arrange
        String invalidEmail = "john@.com";
        //Act and Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new User("John Doe", invalidEmail, "123", "pass", false);
        });
    }

    @Test
    void testSQLInjectionLogin() throws SQLException, ClassNotFoundException, IOException {
        User user = new User("John Doe", "john@example.com", "9101234567", "password", false);
        userDAO.registerUser(user);
        User loggedInUser = userDAO.loginUser("john@example.com' OR '1'='1", "wrongpassword");
        assertNull(loggedInUser);
    }
}
