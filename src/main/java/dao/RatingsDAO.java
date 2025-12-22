package dao;

import models.Ratings;
import utils.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RatingsDAO {

    private static final String RAT_TABLE = "Ratings";

    public int create(Ratings r) throws SQLException {
        String sql = "INSERT INTO " + RAT_TABLE + " (Rating, RatingDate, Comment, CartID) VALUES (?, ?, ?, ?)";

        try (Connection c = DataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, r.getRating());
            ps.setTimestamp(2, r.getRatingDate());
            ps.setString(3, r.getComment());
            ps.setInt(4, r.getCartId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public Ratings findById(int ratingId) throws SQLException {
        String sql = "SELECT RatingID, Rating, RatingDate, Comment, CartID FROM " + RAT_TABLE + " WHERE RatingID = ?";

        try (Connection c = DataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, ratingId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // CartID UNIQUE olduğu için tek kayıt döner
    public Ratings findByCartId(int cartId) throws SQLException {
        String sql = "SELECT RatingID, Rating, RatingDate, Comment, CartID FROM " + RAT_TABLE + " WHERE CartID = ?";

        try (Connection c = DataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, cartId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Ratings> findAll() throws SQLException {
        List<Ratings> list = new ArrayList<>();
        String sql = "SELECT RatingID, Rating, RatingDate, Comment, CartID FROM " + RAT_TABLE + " ORDER BY RatingDate DESC";

        try (Connection c = DataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public void update(Ratings r) throws SQLException {
        String sql = "UPDATE " + RAT_TABLE + " SET Rating = ?, RatingDate = ?, Comment = ? WHERE RatingID = ?";

        try (Connection c = DataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, r.getRating());
            ps.setTimestamp(2, r.getRatingDate());
            ps.setString(3, r.getComment());
            ps.setInt(4, r.getRatingId());

            ps.executeUpdate();
        }
    }

    public void delete(int ratingId) throws SQLException {
        String sql = "DELETE FROM " + RAT_TABLE + " WHERE RatingID = ?";

        try (Connection c = DataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, ratingId);
            ps.executeUpdate();
        }
    }

    private Ratings mapRow(ResultSet rs) throws SQLException {
        return new Ratings(
                rs.getInt("RatingID"),
                rs.getInt("Rating"),
                rs.getTimestamp("RatingDate"),
                rs.getString("Comment"),
                rs.getInt("CartID")
        );
    }
}


