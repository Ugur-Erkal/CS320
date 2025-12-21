package com.cs320.controller.dto;

import java.util.List;

public record RestaurantDetail(
        int restaurantId,
        String restaurantName,
        String cuisineType,
        String address,
        String city,
        List<MenuItemView> menuItems
) {
    public record MenuItemView(
            int menuItemId,
            String name,
            String description,
            double price,
            String image
    ) {}
}
