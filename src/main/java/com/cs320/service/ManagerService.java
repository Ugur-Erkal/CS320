package com.cs320.service;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.cs320.controller.dto.IncomingOrderRow;
import com.cs320.controller.dto.RestaurantOption;

@Service
public class ManagerService {

    private final JdbcTemplate jdbc;

    public ManagerService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

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

}

