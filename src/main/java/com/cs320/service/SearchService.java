package com.cs320.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    private final JdbcTemplate jdbc;

    public SearchService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> searchMenuByCity(String city, String keyword, String sort) {

        String orderBy = switch (sort == null ? "default" : sort) {
            case "priceAsc" -> "m.Price ASC";
            case "priceDesc" -> "m.Price DESC";
            case "nameAsc" -> "m.Name ASC";
            default -> "r.RestaurantName ASC";
        };

        String kw = (keyword == null || keyword.isBlank()) ? null : "%" + keyword.trim() + "%";

        return jdbc.queryForList(
                """
                SELECT
                  r.RestaurantName AS restaurantName,
                  r.City AS city,
                  r.CuisineType AS cuisineType,
                  m.Name AS menuItemName,
                  m.Price AS price,
                  m.Description AS description,
                  m.Image AS image
                FROM Restaurant r
                JOIN Has h ON h.RestaurantID = r.RestaurantID
                JOIN MenuItem m ON m.MenuItemID = h.MenuItemID
                WHERE r.City = ?
                  AND ( ? IS NULL
                        OR m.Name LIKE ?
                        OR m.Description LIKE ?
                        OR r.RestaurantName LIKE ? )
                ORDER BY
                """ + orderBy,
                city, kw, kw, kw, kw
        );


    }
}
