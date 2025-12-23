package com.cs320.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                        FROM `ratings` r
                        JOIN `forrestaurant` fr ON fr.RatingID = r.RatingID
                        JOIN `writtenby` wb ON wb.RatingID = r.RatingID
                        JOIN `user` u ON u.UserID = wb.UserID
                        WHERE fr.RestaurantID = ?
                        ORDER BY r.RatingDate DESC
                        """,
                restaurantId);
    }

    // Ortalama rating hesapla
    public Double getAverageRatingForRestaurant(int restaurantId) {
        return jdbc.queryForObject(
                """
                        SELECT AVG(r.Rating)
                        FROM `ratings` r
                        JOIN `forrestaurant` fr ON fr.RatingID = r.RatingID
                        WHERE fr.RestaurantID = ?
                        """,
                Double.class,
                restaurantId);
    }

    // Yeni rating ekle (restaurant sayfasından)
    @Transactional
    public void addRating(int restaurantId, int userId, int rating, String comment) {

        Timestamp now = new Timestamp(System.currentTimeMillis());
        String cleanComment = (comment == null || comment.isBlank()) ? null : comment.trim();

        jdbc.update(
                """
                        INSERT INTO `ratings` (Rating, RatingDate, Comment)
                        VALUES (?, ?, ?)
                        """,
                rating,
                now,
                cleanComment);

        Integer ratingId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        if (ratingId == null) {
            throw new IllegalStateException("Could not create rating.");
        }

        jdbc.update("INSERT INTO `writtenby` (RatingID, UserID) VALUES (?, ?)", ratingId, userId);
        jdbc.update("INSERT INTO `forrestaurant` (RatingID, RestaurantID) VALUES (?, ?)", ratingId, restaurantId);
    }

    // -------------------------
    // Order (CartID) based rating
    // -------------------------

    public boolean hasRatingForCart(int cartId) {
        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM `ratings` WHERE CartID = ?",
                Integer.class,
                cartId);
        return cnt != null && cnt > 0;
    }

    /**
     * Only allowed for:
     * - carts that belong to user
     * - carts with Status = 'accepted'
     * - carts not already rated (CartID UNIQUE in ratings)
     */
    @Transactional
    public void submitOrderRating(int userId, int cartId, int rating, String comment) {

        // Ownership + accepted + restaurantId
        Integer restaurantId = jdbc.queryForObject(
                """
                        SELECT h.RestaurantID
                        FROM `belongs` b
                        JOIN `cart` c  ON c.CartID = b.CartID
                        JOIN `holds` h ON h.CartID = c.CartID
                        WHERE b.UserID = ?
                          AND c.CartID = ?
                          AND c.`Status` = 'accepted'
                        LIMIT 1
                        """,
                Integer.class,
                userId, cartId);

        if (restaurantId == null) {
            throw new IllegalArgumentException("You can only rate accepted orders that belong to you.");
        }

        // User-friendly pre-check
        if (hasRatingForCart(cartId)) {
            throw new IllegalArgumentException("This order has already been rated.");
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        String cleanComment = (comment == null || comment.isBlank()) ? null : comment.trim();

        try {
            jdbc.update(
                    """
                            INSERT INTO `ratings` (Rating, RatingDate, Comment, CartID)
                            VALUES (?, ?, ?, ?)
                            """,
                    rating, now, cleanComment, cartId);
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("This order has already been rated.");
        }

        Integer ratingId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        if (ratingId == null) {
            throw new IllegalStateException("Could not create rating.");
        }

        jdbc.update("INSERT INTO `writtenby` (RatingID, UserID) VALUES (?, ?)", ratingId, userId);
        jdbc.update("INSERT INTO `forrestaurant` (RatingID, RestaurantID) VALUES (?, ?)", ratingId, restaurantId);
    }

    // ✅ Yeni: My Orders sayfasında formu saklamak için
    public Set<Integer> getRatedCartIdsForUser(int userId) {
        List<Integer> ids = jdbc.query(
                """
                        SELECT r.CartID
                        FROM `ratings` r
                        JOIN `belongs` b ON b.CartID = r.CartID
                        WHERE b.UserID = ?
                          AND r.CartID IS NOT NULL
                        """,
                (rs, rowNum) -> rs.getInt("CartID"),
                userId);
        return new HashSet<>(ids);
    }

    // ✅ Yeni: Kullanıcının cart bazlı review bilgisini göstermek için
    // cartId -> row(rating, comment, ratingDate, cartId)
    public Map<Integer, Map<String, Object>> getRatingsByCartForUser(int userId) {

        List<Map<String, Object>> rows = jdbc.queryForList(
                """
                        SELECT
                            r.CartID     AS cartId,
                            r.Rating     AS rating,
                            r.Comment    AS comment,
                            r.RatingDate AS ratingDate
                        FROM `ratings` r
                        JOIN `belongs` b ON b.CartID = r.CartID
                        WHERE b.UserID = ?
                          AND r.CartID IS NOT NULL
                        ORDER BY r.RatingDate DESC
                        """,
                userId);

        Map<Integer, Map<String, Object>> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object cid = row.get("cartId");
            if (cid instanceof Number) {
                map.put(((Number) cid).intValue(), row);
            }
        }
        return map;
    }

}
