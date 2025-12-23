package com.cs320.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.cs320.controller.dto.OrderItemRow;
import com.cs320.controller.dto.OrderSummary;

@Service
public class OrderHistoryService {

    private final JdbcTemplate jdbc;

    public OrderHistoryService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<OrderSummary> getOrdersForUser(int userId) {

        // ✅ Her kolon için alias veriyoruz -> rs.getX("alias") ile %100 stabil
        List<OrderSummary> orders = jdbc.query("""
                SELECT
                    c.CartID        AS cartId,
                    c.`Status`      AS status,
                    c.AcceptedAt    AS acceptedAt,
                    r.RestaurantID  AS restaurantId,
                    r.RestaurantName AS restaurantName
                FROM `belongs` b
                JOIN `cart` c        ON c.CartID = b.CartID
                JOIN `holds` h       ON h.CartID = c.CartID
                JOIN `restaurant` r  ON r.RestaurantID = h.RestaurantID
                WHERE b.UserID = ?
                  AND c.`Status` IN ('sent', 'accepted')
                ORDER BY c.CartID DESC
                """,
                (rs, rowNum) -> new OrderSummary(
                        rs.getInt("cartId"),
                        rs.getString("status"),
                        rs.getTimestamp("acceptedAt"),
                        rs.getInt("restaurantId"),
                        rs.getString("restaurantName"),
                        new ArrayList<>(),
                        BigDecimal.ZERO),
                userId);

        for (OrderSummary o : orders) {
            List<OrderItemRow> items = jdbc.query("""
                    SELECT
                        mi.MenuItemID AS menuItemId,
                        mi.Name       AS name,
                        mi.Price      AS price,
                        ct.Quantity   AS quantity
                    FROM `contains` ct
                    JOIN `menuitem` mi ON mi.MenuItemID = ct.MenuItemID
                    WHERE ct.CartID = ?
                    ORDER BY mi.Name ASC
                    """,
                    (rs, rowNum) -> new OrderItemRow(
                            rs.getInt("menuItemId"),
                            rs.getString("name"),
                            rs.getBigDecimal("price"),
                            rs.getInt("quantity")),
                    o.cartId());

            BigDecimal total = items.stream()
                    .map(i -> i.price().multiply(BigDecimal.valueOf(i.quantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            o.items().addAll(items);
            o.setTotal(total);
        }

        return orders;
    }
}
