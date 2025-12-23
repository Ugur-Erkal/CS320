package com.cs320.controller.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    @Test
    void testDefaultConstructor() {
        LoginRequest request = new LoginRequest();
        assertNotNull(request);
        assertNull(request.getUsername());
        assertNull(request.getPassword());
    }

    @Test
    void testParameterizedConstructor() {
        String username = "testuser";
        String password = "testpass123";
        LoginRequest request = new LoginRequest(username, password);
        
        assertEquals(username, request.getUsername());
        assertEquals(password, request.getPassword());
    }

    @Test
    void testGettersAndSetters() {
        LoginRequest request = new LoginRequest();
        
        request.setUsername("newuser");
        request.setPassword("newpass123");
        
        assertEquals("newuser", request.getUsername());
        assertEquals("newpass123", request.getPassword());
    }
}

