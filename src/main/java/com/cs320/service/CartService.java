package com.cs320.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CartService {

    private final JdbcTemplate jdbc;

    public CartService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int getOrCreateActiveCart(int userId) {
        Integer existing = jdbc.query(
                """
                SELECT c.CartID
                FROM Cart c
                JOIN Belongs b ON b.CartID = c.CartID
                WHERE b.UserID = ?
                  AND c.Status = 'preparing'
                ORDER BY c.CartID DESC
                LIMIT 1
                """,
                rs -> rs.next() ? rs.getInt("CartID") : null,
                userId
        );

        if (existing != null) return existing;

        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Cart(Status, AcceptedAt) VALUES ('preparing', NULL)",
                    Statement.RETURN_GENERATED_KEYS
            );
            return ps;
        }, kh);

        int cartId = Optional.ofNullable(kh.getKey()).orElseThrow().intValue();
        jdbc.update("INSERT INTO Belongs(CartID, UserID) VALUES (?, ?)", cartId, userId);
        return cartId;
    }

    private void ensureCartRestaurant(int cartId, int restaurantId) {
        Integer existing = jdbc.query(
                "SELECT RestaurantID FROM Holds WHERE CartID = ? LIMIT 1",
                rs -> rs.next() ? rs.getInt("RestaurantID") : null,
                cartId
        );

        if (existing == null) {
            jdbc.update("INSERT INTO Holds(CartID, RestaurantID) VALUES (?, ?)", cartId, restaurantId);
            return;
        }

        if (existing != restaurantId) {
            throw new IllegalStateException("Cart already contains items from another restaurant.");
        }
    }

    public void addToCart(int userId, int menuItemId, int quantity) {
        if (quantity <= 0) quantity = 1;

        int cartId = getOrCreateActiveCart(userId);

        Integer restaurantId = jdbc.queryForObject(
                """
                SELECT h.RestaurantID
                FROM Has h
                WHERE h.MenuItemID = ?
                LIMIT 1
                """,
                Integer.class,
                menuItemId
        );

        if (restaurantId == null) {
            throw new IllegalArgumentException("Menu item not found or not linked to a restaurant.");
        }

        ensureCartRestaurant(cartId, restaurantId);

        jdbc.update(
                """
                INSERT INTO Contains(CartID, MenuItemID, Quantity)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE Quantity = Quantity + VALUES(Quantity)
                """,
                cartId, menuItemId, quantity
        );
    }

    public List<Map<String, Object>> getCartItems(int userId) {
        int cartId = getOrCreateActiveCart(userId);

        return jdbc.queryForList(
                """
                SELECT
                  c.CartID AS cartId,
                  mi.MenuItemID AS menuItemId,
                  mi.Name AS menuItemName,
                  mi.Price AS price,
                  co.Quantity AS quantity,
                  (mi.Price * co.Quantity) AS subtotal,
                  r.RestaurantName AS restaurantName,
                  r.RestaurantID AS restaurantId
                FROM Cart c
                JOIN Belongs b ON b.CartID = c.CartID
                JOIN Contains co ON co.CartID = c.CartID
                JOIN MenuItem mi ON mi.MenuItemID = co.MenuItemID
                JOIN Has h ON h.MenuItemID = mi.MenuItemID
                JOIN Restaurant r ON r.RestaurantID = h.RestaurantID
                WHERE b.UserID = ?
                  AND c.Status = 'preparing'
                ORDER BY r.RestaurantName, mi.Name
                """,
                userId
        );
    }

    public double getCartTotal(int userId) {
        int cartId = getOrCreateActiveCart(userId);

        Double total = jdbc.query(
                """
                SELECT SUM(mi.Price * co.Quantity) AS total
                FROM Contains co
                JOIN MenuItem mi ON mi.MenuItemID = co.MenuItemID
                WHERE co.CartID = ?
                """,
                rs -> rs.next() ? rs.getDouble("total") : 0.0,
                cartId
        );

        return total == null ? 0.0 : total;
    }

    public void removeFromCart(int userId, int menuItemId) {
        int cartId = getOrCreateActiveCart(userId);

        jdbc.update("DELETE FROM Contains WHERE CartID = ? AND MenuItemID = ?", cartId, menuItemId);

        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM Contains WHERE CartID = ?", Integer.class, cartId);
        if (count != null && count == 0) {
            // Cart is empty -> allow choosing a different restaurant next time
            jdbc.update("DELETE FROM Holds WHERE CartID = ?", cartId);
        }
    }

    public void checkout(int userId) {
        int cartId = getOrCreateActiveCart(userId);

        Integer itemCount = jdbc.queryForObject("SELECT COUNT(*) FROM Contains WHERE CartID = ?", Integer.class, cartId);
        if (itemCount == null || itemCount == 0) {
            throw new IllegalStateException("Cart is empty.");
        }

        Integer holdCount = jdbc.queryForObject("SELECT COUNT(*) FROM Holds WHERE CartID = ?", Integer.class, cartId);
        if (holdCount == null || holdCount == 0) {
            throw new IllegalStateException("No restaurant selected for this cart.");
        }

        jdbc.update(
                "UPDATE Cart SET Status = 'sent' WHERE CartID = ? AND Status = 'preparing'",
                cartId
        );
    }
}
