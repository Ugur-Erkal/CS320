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

  /**
   * Finds/creates an ACTIVE cart for a specific restaurant.
   * This allows the user to have multiple "preparing" carts (one per restaurant),
   * without breaking manager/order flows (1 cart belongs to 1 restaurant).
   */
  public int getOrCreateActiveCartForRestaurant(int userId, int restaurantId) {

    Integer existing = jdbc.query("""
        SELECT c.CartID
        FROM Cart c
        JOIN Belongs b ON b.CartID = c.CartID
        JOIN Holds h ON h.CartID = c.CartID
        WHERE b.UserID = ?
          AND c.Status = 'preparing'
          AND h.RestaurantID = ?
        ORDER BY c.CartID DESC
        LIMIT 1
        """,
        rs -> rs.next() ? rs.getInt("CartID") : null,
        userId, restaurantId);

    if (existing != null)
      return existing;

    // create cart
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      PreparedStatement ps = con.prepareStatement(
          "INSERT INTO Cart(Status, AcceptedAt) VALUES ('preparing', NULL)",
          Statement.RETURN_GENERATED_KEYS);
      return ps;
    }, kh);

    int cartId = Optional.ofNullable(kh.getKey()).orElseThrow().intValue();

    // belongs + holds
    jdbc.update("INSERT INTO Belongs(CartID, UserID) VALUES (?, ?)", cartId, userId);
    jdbc.update("INSERT INTO Holds(CartID, RestaurantID) VALUES (?, ?)", cartId, restaurantId);

    return cartId;
  }

  /**
   * Add an item to cart:
   * - Determine restaurantId of menuItem
   * - Use/ create active cart for that restaurant
   * - Upsert into Contains
   */
  public void addToCart(int userId, int menuItemId, int quantity) {
    if (quantity <= 0)
      quantity = 1;

    Integer restaurantId = jdbc.queryForObject("""
        SELECT h.RestaurantID
        FROM Has h
        WHERE h.MenuItemID = ?
        LIMIT 1
        """, Integer.class, menuItemId);

    if (restaurantId == null) {
      throw new IllegalArgumentException("Menu item not found or not linked to a restaurant.");
    }

    int cartId = getOrCreateActiveCartForRestaurant(userId, restaurantId);

    jdbc.update("""
        INSERT INTO Contains(CartID, MenuItemID, Quantity)
        VALUES (?, ?, ?)
        ON DUPLICATE KEY UPDATE Quantity = Quantity + VALUES(Quantity)
        """,
        cartId, menuItemId, quantity);
  }

  /**
   * Return ALL active cart items for the user (across multiple carts),
   * grouped by restaurant/cart in the UI.
   */
  public List<Map<String, Object>> getActiveCartItems(int userId) {
    return jdbc.queryForList("""
        SELECT
          c.CartID AS cartId,
          r.RestaurantID AS restaurantId,
          r.RestaurantName AS restaurantName,

          mi.MenuItemID AS menuItemId,
          mi.Name AS menuItemName,
          mi.Price AS price,

          co.Quantity AS quantity,
          (mi.Price * co.Quantity) AS subtotal
        FROM Cart c
        JOIN Belongs b ON b.CartID = c.CartID
        JOIN Holds ho ON ho.CartID = c.CartID
        JOIN Restaurant r ON r.RestaurantID = ho.RestaurantID

        JOIN Contains co ON co.CartID = c.CartID
        JOIN MenuItem mi ON mi.MenuItemID = co.MenuItemID

        WHERE b.UserID = ?
          AND c.Status = 'preparing'
        ORDER BY r.RestaurantName ASC, c.CartID ASC, mi.Name ASC
        """, userId);
  }

  public double getActiveCartTotal(int userId) {
    Double total = jdbc.query("""
        SELECT SUM(mi.Price * co.Quantity) AS total
        FROM Cart c
        JOIN Belongs b ON b.CartID = c.CartID
        JOIN Contains co ON co.CartID = c.CartID
        JOIN MenuItem mi ON mi.MenuItemID = co.MenuItemID
        WHERE b.UserID = ?
          AND c.Status = 'preparing'
        """,
        rs -> rs.next() ? rs.getDouble("total") : 0.0,
        userId);
    return total == null ? 0.0 : total;
  }

  /**
   * Remove a single menuItem from a specific cart.
   * (Important: now user can have multiple active carts)
   */
  public void removeFromCart(int userId, int cartId, int menuItemId) {

    // ownership guard
    Integer owns = jdbc.query("""
        SELECT 1
        FROM Belongs b
        JOIN Cart c ON c.CartID = b.CartID
        WHERE b.UserID = ?
          AND c.CartID = ?
          AND c.Status = 'preparing'
        LIMIT 1
        """, rs -> rs.next() ? 1 : null, userId, cartId);

    if (owns == null) {
      throw new IllegalStateException("Cart not found or not active.");
    }

    jdbc.update("DELETE FROM Contains WHERE CartID = ? AND MenuItemID = ?", cartId, menuItemId);

    Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM Contains WHERE CartID = ?", Integer.class, cartId);
    if (count != null && count == 0) {
      // cart empty -> clean holds and cart itself (optional)
      jdbc.update("DELETE FROM Holds WHERE CartID = ?", cartId);
      jdbc.update("DELETE FROM Cart WHERE CartID = ?", cartId);
    }
  }

  /**
   * Checkout a specific cart.
   */
  public void checkoutCart(int userId, int cartId) {

    // ownership + preparing guard
    Integer ok = jdbc.query("""
        SELECT 1
        FROM Belongs b
        JOIN Cart c ON c.CartID = b.CartID
        WHERE b.UserID = ?
          AND c.CartID = ?
          AND c.Status = 'preparing'
        LIMIT 1
        """, rs -> rs.next() ? 1 : null, userId, cartId);

    if (ok == null) {
      throw new IllegalStateException("Cart not found or not active.");
    }

    Integer itemCount = jdbc.queryForObject("SELECT COUNT(*) FROM Contains WHERE CartID = ?", Integer.class, cartId);
    if (itemCount == null || itemCount == 0) {
      throw new IllegalStateException("Cart is empty.");
    }

    Integer holdCount = jdbc.queryForObject("SELECT COUNT(*) FROM Holds WHERE CartID = ?", Integer.class, cartId);
    if (holdCount == null || holdCount == 0) {
      throw new IllegalStateException("No restaurant selected for this cart.");
    }

    jdbc.update("""
        UPDATE Cart
        SET Status = 'sent'
        WHERE CartID = ?
          AND Status = 'preparing'
        """, cartId);
  }
}
