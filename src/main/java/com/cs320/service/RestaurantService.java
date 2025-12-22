package com.cs320.service;

import com.cs320.controller.dto.RestaurantDetail;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RestaurantService {

    private final JdbcTemplate jdbc;

    public RestaurantService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<RestaurantDetail> getRestaurantDetail(int restaurantId, String keyword) {
        // 1) restaurant info
        var info = jdbc.query(
                """
                SELECT RestaurantID, RestaurantName, CuisineType, Address, City
                FROM Restaurant
                WHERE RestaurantID = ?
                """,
                rs -> {
                    if (!rs.next()) return Optional.<RestaurantDetail>empty();
                    return Optional.of(new RestaurantDetail(
                            rs.getInt("RestaurantID"),
                            rs.getString("RestaurantName"),
                            rs.getString("CuisineType"),
                            rs.getString("Address"),
                            rs.getString("City"),
                            List.of()
                    ));
                },
                restaurantId
        );

        if (info.isEmpty()) return Optional.empty();

        String kw = (keyword == null || keyword.isBlank()) ? null : "%" + keyword.trim() + "%";

        // 2) menu items (optionally filtered)
        List<RestaurantDetail.MenuItemView> items = jdbc.query(
                """
                SELECT m.MenuItemID, m.Name, m.Description, m.Price, m.Image
                FROM Has h
                JOIN MenuItem m ON m.MenuItemID = h.MenuItemID
                WHERE h.RestaurantID = ?
                  AND (? IS NULL OR m.Name LIKE ? OR m.Description LIKE ?)
                ORDER BY m.Name ASC
                """,
                (rs, rowNum) -> new RestaurantDetail.MenuItemView(
                        rs.getInt("MenuItemID"),
                        rs.getString("Name"),
                        rs.getString("Description"),
                        rs.getDouble("Price"),
                        rs.getString("Image")
                ),
                restaurantId, kw, kw, kw
        );

        var base = info.get();
        return Optional.of(new RestaurantDetail(
                base.restaurantId(),
                base.restaurantName(),
                base.cuisineType(),
                base.address(),
                base.city(),
                items
        ));
    }
}
