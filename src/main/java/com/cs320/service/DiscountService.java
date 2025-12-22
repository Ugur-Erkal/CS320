package com.cs320.service;

import com.cs320.controller.dto.MenuItemOption;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class DiscountService {

    private final JdbcTemplate jdbc;

    public DiscountService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Manager bu restaurant'ı yönetiyor mu?
    public boolean isManagerOfRestaurant(int managerUserId, int restaurantId) {
        Integer cnt = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM Manages
                WHERE UserID = ? AND RestaurantID = ?
                """,
                Integer.class,
                managerUserId, restaurantId
        );
        return cnt != null && cnt > 0;
    }

    // MenuItem gerçekten bu restaurant'a mı ait?
    public boolean isMenuItemOwnedByRestaurant(int menuItemId, int restaurantId) {
        Integer cnt = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM Has
                WHERE RestaurantID = ? AND MenuItemID = ?
                """,
                Integer.class,
                restaurantId, menuItemId
        );
        return cnt != null && cnt > 0;
    }

    // Restaurant'ın menu item listesini (dropdown için) getir
    public List<MenuItemOption> getMenuItemsForRestaurant(int restaurantId) {
        return jdbc.query(
                """
                SELECT mi.MenuItemID, mi.Name
                FROM Has h
                JOIN MenuItem mi ON mi.MenuItemID = h.MenuItemID
                WHERE h.RestaurantID = ?
                ORDER BY mi.Name ASC
                """,
                (rs, rowNum) -> new MenuItemOption(
                        rs.getInt("MenuItemID"),
                        rs.getString("Name")
                ),
                restaurantId
        );
    }

    // DiscountID varsa onu kullan, yoksa oluştur
    private int getOrCreateDiscountId(double discountPercent) {
        Integer existing = jdbc.query(
                """
                SELECT DiscountID
                FROM Discount
                WHERE Discount = ?
                LIMIT 1
                """,
                rs -> rs.next() ? rs.getInt("DiscountID") : null,
                discountPercent
        );

        if (existing != null) return existing;

        jdbc.update("INSERT INTO Discount(Discount) VALUES (?)", discountPercent);

        Integer id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        if (id == null) throw new IllegalStateException("Failed to create Discount row.");
        return id;
    }

    // Applied'a yeni indirim ekle
    public void applyDiscount(int menuItemId, double discountPercent, LocalDateTime start, LocalDateTime end) {
        if (discountPercent <= 0 || discountPercent >= 100) {
            throw new IllegalArgumentException("Discount must be between 0 and 100.");
        }
        if (start == null || end == null || !end.isAfter(start)) {
            throw new IllegalArgumentException("End date must be after start date.");
        }

        int discountId = getOrCreateDiscountId(discountPercent);

        jdbc.update(
                """
                INSERT INTO Applied(DiscountID, MenuItemID, StartDate, EndDate)
                VALUES (?, ?, ?, ?)
                """,
                discountId,
                menuItemId,
                Timestamp.valueOf(start),
                Timestamp.valueOf(end)
        );
    }

    // Manager dashboard'da göstermek için indirimleri listele
    public List<Map<String, Object>> getDiscountsForRestaurant(int restaurantId) {
        return jdbc.queryForList(
                """
                SELECT
                  mi.MenuItemID AS menuItemId,
                  mi.Name AS menuItemName,
                  d.Discount AS discountPercent,
                  ap.StartDate AS startDate,
                  ap.EndDate AS endDate
                FROM Has h
                JOIN MenuItem mi ON mi.MenuItemID = h.MenuItemID
                JOIN Applied ap ON ap.MenuItemID = mi.MenuItemID
                JOIN Discount d ON d.DiscountID = ap.DiscountID
                WHERE h.RestaurantID = ?
                ORDER BY ap.EndDate DESC, mi.Name ASC
                """,
                restaurantId
        );
    }

    public boolean menuItemBelongsToRestaurant(int menuItemId, int restaurantId) {
        Integer exists = jdbc.query(
                """
                SELECT 1
                FROM Has
                WHERE RestaurantID = ? AND MenuItemID = ?
                LIMIT 1
                """,
                rs -> rs.next() ? 1 : null,
                restaurantId, menuItemId
        );
        return exists != null;
    }

}
