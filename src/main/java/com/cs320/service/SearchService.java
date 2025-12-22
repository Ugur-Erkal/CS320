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
            case "priceAsc" -> "discountedPrice ASC, m.Price ASC";
            case "priceDesc" -> "discountedPrice DESC, m.Price DESC";
            case "nameAsc" -> "m.Name ASC";
            default -> "r.RestaurantName ASC, m.Name ASC";
        };

        String kw = (keyword == null || keyword.isBlank()) ? null : "%" + keyword.trim() + "%";

        return jdbc.queryForList(
                ("""
                SELECT
                  r.RestaurantID AS restaurantId,
                  r.RestaurantName AS restaurantName,
                  r.City AS city,
                  r.CuisineType AS cuisineType,

                  m.MenuItemID AS menuItemId,
                  m.Name AS menuItemName,
                  m.Price AS price,
                  m.Description AS description,
                  m.Image AS image,

                  d.Discount AS discountPercent,

                  CASE
                    WHEN d.Discount IS NULL THEN NULL
                    ELSE ROUND(m.Price * (1 - (d.Discount / 100)), 2)
                  END AS discountedPrice

                FROM Restaurant r
                JOIN Has h ON h.RestaurantID = r.RestaurantID
                JOIN MenuItem m ON m.MenuItemID = h.MenuItemID

                LEFT JOIN (
                    SELECT ap.MenuItemID, ap.DiscountID
                    FROM Applied ap
                    WHERE NOW() BETWEEN ap.StartDate AND ap.EndDate
                ) apActive ON apActive.MenuItemID = m.MenuItemID
                LEFT JOIN Discount d ON d.DiscountID = apActive.DiscountID

                WHERE r.City = ?
                  AND ( ? IS NULL
                        OR m.Name LIKE ?
                        OR m.Description LIKE ?
                        OR r.RestaurantName LIKE ? )

                ORDER BY
                """ + orderBy),
                city, kw, kw, kw, kw
        );
    }
}
