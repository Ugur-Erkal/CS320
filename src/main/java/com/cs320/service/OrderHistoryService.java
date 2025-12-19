package com.cs320.service;

import com.cs320.controller.dto.OrderItemRow;
import com.cs320.controller.dto.OrderSummary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderHistoryService {

    private final JdbcTemplate jdbc;

    public OrderHistoryService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<OrderSummary> getOrdersForUser(int userId) {
         List<OrderSummary> orders = jdbc.query("""
                SELECT c.CartID, c.Status, c.AcceptedAt,
                       r.RestaurantID, r.RestaurantName
                FROM Belongs b
                JOIN Cart c ON c.CartID = b.CartID
                JOIN Holds h ON h.CartID = c.CartID
                JOIN Restaurant r ON r.RestaurantID = h.RestaurantID
                WHERE b.UserID = ?
                ORDER BY c.CartID DESC
                """,
                (rs, rowNum) -> new OrderSummary(
                        rs.getInt("CartID"),
                        rs.getString("Status"),
                        rs.getTimestamp("AcceptedAt"),
                        rs.getInt("RestaurantID"),
                        rs.getString("RestaurantName"),
                        new ArrayList<>(),
                        BigDecimal.ZERO
                ),
                userId
        );
        for (OrderSummary o : orders) {
            List<OrderItemRow> items = jdbc.query("""
                    SELECT mi.MenuItemID, mi.Name, mi.Price, ct.Quantity
                    FROM Contains ct
                    JOIN MenuItem mi ON mi.MenuItemID = ct.MenuItemID
                    WHERE ct.CartID = ?
                    ORDER BY mi.Name ASC
                    """,
                    (rs, rowNum) -> new OrderItemRow(
                            rs.getInt("MenuItemID"),
                            rs.getString("Name"),
                            rs.getBigDecimal("Price"),
                            rs.getInt("Quantity")
                    ),
                    o.cartId()
            );

            BigDecimal total = items.stream()
                    .map(i -> i.price().multiply(BigDecimal.valueOf(i.quantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            o.items().addAll(items);
            o.setTotal(total);
        }

        return orders;
    }
}
