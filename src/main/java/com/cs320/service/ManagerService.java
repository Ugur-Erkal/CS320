package com.cs320.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.cs320.controller.dto.IncomingOrderRow;
import com.cs320.controller.dto.RestaurantOption;
import com.cs320.controller.dto.MenuItemRow;

@Service
public class ManagerService {

    private final JdbcTemplate jdbc;

    public ManagerService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // -----------------------------
    // Existing: managed restaurants
    // -----------------------------
    public List<RestaurantOption> getManagedRestaurants(int managerUserId) {
        return jdbc.query("""
                SELECT r.RestaurantID, r.RestaurantName
                FROM Manages m
                JOIN Restaurant r ON r.RestaurantID = m.RestaurantID
                WHERE m.UserID = ?
                ORDER BY r.RestaurantName ASC
                """,
                (rs, rowNum) -> new RestaurantOption(
                        rs.getInt("RestaurantID"),
                        rs.getString("RestaurantName")
                ),
                managerUserId
        );
    }

    // -----------------------------
    // NEW: create restaurant and bind to manager
    // -----------------------------
    public int createRestaurant(int managerUserId,
                                String restaurantName,
                                String address,
                                String city,
                                String cuisineType) {

        jdbc.update("""
                INSERT INTO Restaurant (RestaurantName, Address, City, CuisineType)
                VALUES (?, ?, ?, ?)
                """,
                restaurantName, address, city, cuisineType
        );

        Integer restaurantId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);

        jdbc.update("""
                INSERT INTO Manages (UserID, RestaurantID)
                VALUES (?, ?)
                """, managerUserId, restaurantId);

        return restaurantId;
    }

    // -----------------------------
    // Existing: incoming orders for restaurant
    // -----------------------------
    public List<IncomingOrderRow> getIncomingOrdersForRestaurant(int restaurantId) {
        return jdbc.query("""
                SELECT c.CartID,
                       u.Username AS CustomerUsername,
                       mi.Name AS ItemName,
                       ct.Quantity,
                       c.Status,
                       (mi.Price * ct.Quantity) AS LineTotal
                FROM Holds h
                JOIN Cart c ON c.CartID = h.CartID
                JOIN Belongs b ON b.CartID = c.CartID
                JOIN User u ON u.UserID = b.UserID
                JOIN Contains ct ON ct.CartID = c.CartID
                JOIN MenuItem mi ON mi.MenuItemID = ct.MenuItemID
                WHERE h.RestaurantID = ?
                    AND c.Status = 'sent'
                ORDER BY c.CartID DESC, mi.Name ASC
                """,
                (rs, rowNum) -> new IncomingOrderRow(
                        rs.getInt("CartID"),
                        rs.getString("CustomerUsername"),
                        rs.getString("ItemName"),
                        rs.getInt("Quantity"),
                        rs.getString("Status"),
                        rs.getBigDecimal("LineTotal")
                ),
                restaurantId
        );
    }

    public void acceptOrder(int cartId) {
        jdbc.update("""
                UPDATE Cart
                SET Status = 'accepted', AcceptedAt = NOW()
                WHERE CartID = ?
                """, cartId);
    }

    public boolean managesRestaurant(int managerUserId, int restaurantId) {
        Integer exists = jdbc.query(
                """
                SELECT 1
                FROM Manages
                WHERE UserID = ? AND RestaurantID = ?
                LIMIT 1
                """,
                rs -> rs.next() ? 1 : null,
                managerUserId, restaurantId
        );
        return exists != null;
    }

    // -----------------------------
    // NEW: restaurant manage page helpers
    // -----------------------------
    public RestaurantDetails getRestaurantDetails(int restaurantId) {
        return jdbc.queryForObject("""
                SELECT RestaurantID, RestaurantName, Address, City, CuisineType
                FROM Restaurant
                WHERE RestaurantID = ?
                """,
                (rs, rowNum) -> new RestaurantDetails(
                        rs.getInt("RestaurantID"),
                        rs.getString("RestaurantName"),
                        rs.getString("Address"),
                        rs.getString("City"),
                        rs.getString("CuisineType")
                ),
                restaurantId
        );
    }

    public List<MenuItemRow> getMenuItemsForRestaurant(int restaurantId) {
        return jdbc.query("""
                SELECT mi.MenuItemID, mi.Name, mi.Price, mi.Description, mi.Image
                FROM Has h
                JOIN MenuItem mi ON mi.MenuItemID = h.MenuItemID
                WHERE h.RestaurantID = ?
                ORDER BY mi.Name ASC
                """,
                (rs, rowNum) -> new MenuItemRow(
                        rs.getInt("MenuItemID"),
                        rs.getString("Name"),
                        rs.getBigDecimal("Price"),
                        rs.getString("Description"),
                        rs.getString("Image")
                ),
                restaurantId
        );
    }

    public List<String> getKeywordsForRestaurant(int restaurantId) {
        return jdbc.query("""
                SELECT k.Keyword
                FROM AssociatedWith aw
                JOIN Keyword k ON k.KeywordID = aw.KeywordID
                WHERE aw.RestaurantID = ?
                ORDER BY k.Keyword ASC
                """,
                (rs, rowNum) -> rs.getString("Keyword"),
                restaurantId
        );
    }

    // -----------------------------
    // NEW: add keyword (create if needed) + associate
    // -----------------------------
    public void addKeywordToRestaurant(int managerUserId, int restaurantId, String keyword) {
        if (!managesRestaurant(managerUserId, restaurantId)) {
            throw new IllegalArgumentException("Unauthorized restaurant access.");
        }

        // 1) find or insert keyword
        Integer keywordId = jdbc.query("""
                SELECT KeywordID FROM Keyword WHERE Keyword = ? LIMIT 1
                """,
                rs -> rs.next() ? rs.getInt("KeywordID") : null,
                keyword
        );

        if (keywordId == null) {
            jdbc.update("INSERT INTO Keyword (Keyword) VALUES (?)", keyword);
            keywordId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        }

        // 2) associate (ignore duplicates safely)
        try {
            jdbc.update("""
                    INSERT INTO AssociatedWith (RestaurantID, KeywordID)
                    VALUES (?, ?)
                    """, restaurantId, keywordId);
        } catch (DuplicateKeyException ignore) {
            // already associated, do nothing
        }
    }

    // -----------------------------
    // NEW: add menu item + link to restaurant
    // -----------------------------
    public void addMenuItemToRestaurant(int managerUserId,
                                        int restaurantId,
                                        String name,
                                        BigDecimal price,
                                        String description,
                                        String imageUrl) {

        if (!managesRestaurant(managerUserId, restaurantId)) {
            throw new IllegalArgumentException("Unauthorized restaurant access.");
        }

        jdbc.update("""
                INSERT INTO MenuItem (Description, Price, Name, Image)
                VALUES (?, ?, ?, ?)
                """, description, price, name, imageUrl);

        Integer menuItemId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);

        jdbc.update("""
                INSERT INTO Has (RestaurantID, MenuItemID)
                VALUES (?, ?)
                """, restaurantId, menuItemId);
    }

    // Small internal record for restaurant header
    public record RestaurantDetails(
            int restaurantId,
            String restaurantName,
            String address,
            String city,
            String cuisineType
    ) {}
}
