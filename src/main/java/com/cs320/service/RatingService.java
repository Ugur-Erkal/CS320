package com.cs320.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class RatingService {

    private final JdbcTemplate jdbc;

    public RatingService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Restaurant için tüm rating + comment’leri getir
    public List<Map<String, Object>> getRatingsForRestaurant(int restaurantId) {
        return jdbc.queryForList(
                """
                SELECT
                  r.Rating,
                  r.Comment,
                  r.RatingDate,
                  u.Username
                FROM Ratings r
                JOIN ForRestaurant fr ON fr.RatingID = r.RatingID
                JOIN WrittenBy wb ON wb.RatingID = r.RatingID
                JOIN User u ON u.UserID = wb.UserID
                WHERE fr.RestaurantID = ?
                ORDER BY r.RatingDate DESC
                """,
                restaurantId
        );
    }

    // Ortalama rating hesapla
    public Double getAverageRatingForRestaurant(int restaurantId) {
        return jdbc.queryForObject(
                """
                SELECT AVG(r.Rating)
                FROM Ratings r
                JOIN ForRestaurant fr ON fr.RatingID = r.RatingID
                WHERE fr.RestaurantID = ?
                """,
                Double.class,
                restaurantId
        );
    }

    // Yeni rating ekle (restaurant sayfasından)
    public void addRating(int restaurantId, int userId, int rating, String comment) {
        jdbc.update(
                """
                INSERT INTO Ratings (Rating, RatingDate, Comment)
                VALUES (?, ?, ?)
                """,
                rating,
                LocalDateTime.now(),
                comment
        );

        Integer ratingId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);

        jdbc.update("INSERT INTO WrittenBy (RatingID, UserID) VALUES (?, ?)", ratingId, userId);
        jdbc.update("INSERT INTO ForRestaurant (RatingID, RestaurantID) VALUES (?, ?)", ratingId, restaurantId);
    }

    // -------------------------
    // Order (CartID) based rating
    // -------------------------

    public boolean hasRatingForCart(int cartId) {
        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM Ratings WHERE CartID = ?",
                Integer.class,
                cartId
        );
        return cnt != null && cnt > 0;
    }

    /**
     * Only allowed for:
     * - carts that belong to user
     * - carts with Status = 'accepted'
     * - carts not already rated (CartID UNIQUE in Ratings)
     */
    public void submitOrderRating(int userId, int cartId, int rating, String comment) {

        // 0) Verify ownership + accepted + get restaurantId
        Integer restaurantId = jdbc.queryForObject(
                """
                SELECT h.RestaurantID
                FROM Belongs b
                JOIN Cart c ON c.CartID = b.CartID
                JOIN Holds h ON h.CartID = c.CartID
                WHERE b.UserID = ?
                  AND c.CartID = ?
                  AND c.Status = 'accepted'
                LIMIT 1
                """,
                Integer.class,
                userId, cartId
        );

        if (restaurantId == null) {
            throw new IllegalStateException("You can only rate accepted orders that belong to you.");
        }

        try {
            // 1) Insert into Ratings with CartID (unique)
            jdbc.update(
                    """
                    INSERT INTO Ratings (Rating, RatingDate, Comment, CartID)
                    VALUES (?, ?, ?, ?)
                    """,
                    rating,
                    LocalDateTime.now(),
                    (comment == null || comment.isBlank()) ? null : comment.trim(),
                    cartId
            );
        } catch (DuplicateKeyException ex) {
            throw new IllegalStateException("This order has already been rated.");
        }

        Integer ratingId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);

        // 2) WrittenBy
        jdbc.update("INSERT INTO WrittenBy (RatingID, UserID) VALUES (?, ?)", ratingId, userId);

        // 3) ForRestaurant
        jdbc.update("INSERT INTO ForRestaurant (RatingID, RestaurantID) VALUES (?, ?)", ratingId, restaurantId);
    }
}
