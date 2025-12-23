package com.cs320.controller.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CartItemTest {

    @Test
    void testConstructor() {
        CartItem item = new CartItem(1, "Pizza", 15.99, 2);
        
        assertEquals(1, item.getItemId());
        assertEquals("Pizza", item.getItemName());
        assertEquals(15.99, item.getPrice(), 0.001);
        assertEquals(2, item.getQuantity());
    }

    @Test
    void testGetters() {
        CartItem item = new CartItem(2, "Burger", 8.50, 3);
        
        assertEquals(2, item.getItemId());
        assertEquals("Burger", item.getItemName());
        assertEquals(8.50, item.getPrice(), 0.001);
        assertEquals(3, item.getQuantity());
    }

    @Test
    void testIncreaseQuantity() {
        CartItem item = new CartItem(1, "Pizza", 15.99, 2);
        
        assertEquals(2, item.getQuantity());
        item.increaseQuantity();
        assertEquals(3, item.getQuantity());
        item.increaseQuantity();
        assertEquals(4, item.getQuantity());
    }

    @Test
    void testGetTotalPrice() {
        CartItem item = new CartItem(1, "Pizza", 15.99, 2);
        
        double expectedTotal = 15.99 * 2;
        assertEquals(expectedTotal, item.getTotalPrice(), 0.001);
        
        item.increaseQuantity();
        expectedTotal = 15.99 * 3;
        assertEquals(expectedTotal, item.getTotalPrice(), 0.001);
    }
}

