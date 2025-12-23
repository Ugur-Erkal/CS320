package com.cs320.controller.dto;

import java.math.BigDecimal;

public record MenuItemRow(
        int menuItemId,
        String name,
        BigDecimal price,
        String description,
        String image
) {}
