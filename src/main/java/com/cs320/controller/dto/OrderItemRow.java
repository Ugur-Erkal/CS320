package com.cs320.controller.dto;

import java.math.BigDecimal;

public record OrderItemRow(
        int menuItemId,
        String name,
        BigDecimal price,
        int quantity
) {
    public BigDecimal lineTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
