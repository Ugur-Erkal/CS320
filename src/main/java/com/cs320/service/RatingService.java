package com.cs320.service;

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

    // Yeni rating ekle
    public void addRating(int restaurantId, int userId, int rating, String comment) {

        // 1) Ratings tablosuna ekle
        jdbc.update(
                """
                INSERT INTO Ratings (Rating, RatingDate, Comment)
                VALUES (?, ?, ?)
                """,
                rating,
                LocalDateTime.now(),
                comment
        );

        // 2) Eklenen rating’in id’sini al
        Integer ratingId = jdbc.queryForObject(
                "SELECT LAST_INSERT_ID()",
                Integer.class
        );

        // 3) WrittenBy
        jdbc.update(
                "INSERT INTO WrittenBy (RatingID, UserID) VALUES (?, ?)",
                ratingId, userId
        );

        // 4) ForRestaurant
        jdbc.update(
                "INSERT INTO ForRestaurant (RatingID, RestaurantID) VALUES (?, ?)",
                ratingId, restaurantId
        );
    }
}
