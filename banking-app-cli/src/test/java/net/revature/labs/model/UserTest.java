package net.revature.labs.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User("John Doe", "john@example.com", "1234567890", "password", false);
    }

    @Test
    public void testConstructor() {
        assertEquals(0, user.getUserId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("1234567890", user.getPhone());
        assertEquals("password", user.getPassword());
        assertFalse(user.isAdmin());
    }

    @Test
    public void testToString() {
        String expectedToString = "User{id=0, name='John Doe', email='john@example.com', phone='1234567890', admin=false}";
        assertEquals(expectedToString, user.toString());
    }

    @Test
    public void testValidation() {
        assertTrue(User.isValidName("John Doe"));
        assertFalse(User.isValidName("John123")); // Invalid name

        assertTrue(User.isValidEmail("john@example.com"));
        assertFalse(User.isValidEmail("john@example.com.")); // Invalid email
    }

    @Test
    public void testUserModel() {
        // Test case 1: Create a user and verify getters
        assertEquals(0, user.getUserId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("1234567890", user.getPhone());
        assertEquals("password", user.getPassword());
        assertFalse(user.isAdmin());

        // Test case 2: Test setters
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setPhone("0987654321");
        user.setPassword("newpassword");
        user.setAdmin(true);

        assertEquals("Jane Doe", user.getName());
        assertEquals("jane@example.com", user.getEmail());
        assertEquals("0987654321", user.getPhone());
        assertEquals("newpassword", user.getPassword());
        assertTrue(user.isAdmin());
    }

    @Test
    public void testNullInputs() {
        assertThrows(IllegalArgumentException.class, () -> new User(null, "john@example.com", "1234567890", "password", false));
        assertThrows(IllegalArgumentException.class, () -> new User("John Doe", null, "1234567890", "password", false));
        assertThrows(IllegalArgumentException.class, () -> new User( "John Doe", "john@example.com", null, "password", false));
        assertThrows(IllegalArgumentException.class, () -> new User("John Doe", "john@example.com", "1234567890", null, false));
    }
}
