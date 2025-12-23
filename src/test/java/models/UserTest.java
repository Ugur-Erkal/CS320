package models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testConstructorWithUserId() {
        User user = new User(1, "testuser", "password123", "Customer");
        
        assertEquals(1, user.getUserId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("Customer", user.getUserType());
    }

    @Test
    void testConstructorWithoutUserId() {
        User user = new User("testuser", "password123", "Manager");
        
        assertEquals(0, user.getUserId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("Manager", user.getUserType());
    }

    @Test
    void testSetters() {
        User user = new User(1, "testuser", "password123", "Customer");
        
        user.setUserId(2);
        user.setUsername("newuser");
        user.setPassword("newpass");
        user.setUserType("Manager");
        
        assertEquals(2, user.getUserId());
        assertEquals("newuser", user.getUsername());
        assertEquals("newpass", user.getPassword());
        assertEquals("Manager", user.getUserType());
    }

    @Test
    void testEquals() {
        User user1 = new User(1, "testuser", "password123", "Customer");
        User user2 = new User(1, "different", "different", "Manager");
        User user3 = new User(2, "testuser", "password123", "Customer");
        
        assertEquals(user1, user2); // Same userId
        assertNotEquals(user1, user3); // Different userId
        assertEquals(user1, user1); // Same instance
    }

    @Test
    void testHashCode() {
        User user1 = new User(1, "testuser", "password123", "Customer");
        User user2 = new User(1, "different", "different", "Manager");
        
        assertEquals(user1.hashCode(), user2.hashCode()); // Same userId = same hashCode
    }

    @Test
    void testToString() {
        User user = new User(1, "testuser", "password123", "Customer");
        String result = user.toString();
        
        assertTrue(result.contains("User{"));
        assertTrue(result.contains("userId=1"));
        assertTrue(result.contains("username='testuser'"));
        assertTrue(result.contains("userType='Customer'"));
    }
}

