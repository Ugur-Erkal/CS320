package com.cs320.controller.dto;

import java.math.BigDecimal;

public class IncomingOrderRow {
    private final int cartId;
    private final String customerUsername;
    private final String itemName;
    private final int quantity;
    private final String status;
    private final BigDecimal lineTotal;

    public IncomingOrderRow(int cartId, String customerUsername, String itemName,
                            int quantity, String status, BigDecimal lineTotal) {
        this.cartId = cartId;
        this.customerUsername = customerUsername;
        this.itemName = itemName;
        this.quantity = quantity;
        this.status = status;
        this.lineTotal = lineTotal;
    }

    public int getCartId() { return cartId; }
    public String getCustomerUsername() { return customerUsername; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public String getStatus() { return status; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
