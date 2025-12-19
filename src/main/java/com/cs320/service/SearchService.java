package com.cs320.service;

import com.cs320.controller.dto.SearchResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private final JdbcTemplate jdbc;

    public SearchService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<SearchResult> searchMenuByCity(String city, String keyword) {

        String searchTerm = (keyword == null || keyword.isBlank())
                ? "%"
                : "%" + keyword.trim() + "%";

        String sql = """
            SELECT 
                R.RestaurantID,
                R.RestaurantName,
                R.CuisineType,
                R.Address,
                R.City,
                M.MenuItemID,
                M.Name AS MenuItem,
                M.Description,
                M.Price,
                M.Image,
                COUNT(DISTINCT Ra.RatingID) AS RatingCount,
                AVG(Ra.Rating) AS AvgRating,
                (
                    SELECT D.Discount
                    FROM Applied A2
                    JOIN Discount D ON A2.DiscountID = D.DiscountID
                    WHERE A2.MenuItemID = M.MenuItemID
                      AND NOW() BETWEEN A2.StartDate AND A2.EndDate
                    ORDER BY A2.StartDate DESC
                    LIMIT 1
                ) AS ActiveDiscount
            FROM Restaurant R
            JOIN Has H ON R.RestaurantID = H.RestaurantID
            JOIN MenuItem M ON H.MenuItemID = M.MenuItemID
            LEFT JOIN ForRestaurant FR ON FR.RestaurantID = R.RestaurantID
            LEFT JOIN Ratings Ra ON FR.RatingID = Ra.RatingID
            LEFT JOIN AssociatedWith AW ON R.RestaurantID = AW.RestaurantID
            LEFT JOIN Keyword K ON AW.KeywordID = K.KeywordID
            WHERE (
                R.RestaurantName LIKE ?
                OR M.Name LIKE ?
                OR M.Description LIKE ?
                OR K.Keyword LIKE ?
            )
            AND R.City = ?
            GROUP BY R.RestaurantID, M.MenuItemID
            ORDER BY (AvgRating IS NULL), AvgRating DESC
        """;

        return jdbc.query(sql, (rs, rowNum) -> {
            SearchResult r = new SearchResult();
            r.setMenuItemId(rs.getInt("MenuItemID"));
            r.setMenuItem(rs.getString("MenuItem"));
            r.setDescription(rs.getString("Description"));
            r.setPrice(rs.getDouble("Price"));
            r.setImage(rs.getString("Image"));
            r.setRestaurantName(rs.getString("RestaurantName"));
            r.setCuisineType(rs.getString("CuisineType"));
            r.setAddress(rs.getString("Address"));
            r.setCity(rs.getString("City"));
            r.setRatingCount(rs.getLong("RatingCount"));
            r.setAvgRating(rs.getDouble("AvgRating"));

            Double discount = rs.getObject("ActiveDiscount", Double.class);
            if (discount != null) {
                double discounted = r.getPrice() * (1 - discount / 100);
                r.setDiscountedPrice(Math.round(discounted * 100.0) / 100.0);
            }

            return r;
        }, searchTerm, searchTerm, searchTerm, searchTerm, city);
    }
}
