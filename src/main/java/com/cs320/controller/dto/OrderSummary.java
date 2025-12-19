package com.cs320.controller.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public class OrderSummary {
    private final int cartId;
    private final String status;
    private final Timestamp acceptedAt;
    private final int restaurantId;
    private final String restaurantName;
    private final List<OrderItemRow> items;
    private BigDecimal total;

    public OrderSummary(int cartId, String status, Timestamp acceptedAt,
                        int restaurantId, String restaurantName,
                        List<OrderItemRow> items, BigDecimal total) {
        this.cartId = cartId;
        this.status = status;
        this.acceptedAt = acceptedAt;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.items = items;
        this.total = total;
    }

    public int cartId() { return cartId; }
    public String status() { return status; }
    public Timestamp acceptedAt() { return acceptedAt; }
    public int restaurantId() { return restaurantId; }
    public String restaurantName() { return restaurantName; }
    public List<OrderItemRow> items() { return items; }

    public BigDecimal total() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}

