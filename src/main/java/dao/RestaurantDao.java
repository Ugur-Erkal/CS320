package dao;

import models.Restaurant;
import utils.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RestaurantDao {

    private static final String REST_TABLE = "Restaurant";

    public int create(Restaurant r) throws SQLException {
        String sql = "INSERT INTO " + REST_TABLE +
                " (RestaurantName, Address, City, CuisineType) VALUES (?, ?, ?, ?)";

        try (Connection c = DataSource.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, r.getRestaurantName());
            ps.setString(2, r.getAddress());
            ps.setString(3, r.getCity());
            ps.setString(4, r.getCuisineType()); // null olabilir

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public Restaurant findById(int restaurantId) throws SQLException {
        String sql = "SELECT RestaurantID, RestaurantName, Address, City, CuisineType FROM " +
                REST_TABLE + " WHERE RestaurantID = ?";

        try (Connection c = DataSource.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, restaurantId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        }
        return null;
    }

    public List<Restaurant> findAll() throws SQLException {
        List<Restaurant> list = new ArrayList<>();
        String sql = "SELECT RestaurantID, RestaurantName, Address, City, CuisineType FROM " +
                REST_TABLE + " ORDER BY RestaurantName";

        try (Connection c = DataSource.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next())
                list.add(mapRow(rs));
        }
        return list;
    }

    public void update(Restaurant r) throws SQLException {
        String sql = "UPDATE " + REST_TABLE +
                " SET RestaurantName = ?, Address = ?, City = ?, CuisineType = ? WHERE RestaurantID = ?";

        try (Connection c = DataSource.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, r.getRestaurantName());
            ps.setString(2, r.getAddress());
            ps.setString(3, r.getCity());
            ps.setString(4, r.getCuisineType());
            ps.setInt(5, r.getRestaurantId());

            ps.executeUpdate();
        }
    }

    public void delete(int restaurantId) throws SQLException {
        String sql = "DELETE FROM " + REST_TABLE + " WHERE RestaurantID = ?";

        try (Connection c = DataSource.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, restaurantId);
            ps.executeUpdate();
        }
    }

    private Restaurant mapRow(ResultSet rs) throws SQLException {
        return new Restaurant(
                rs.getInt("RestaurantID"),
                rs.getString("RestaurantName"),
                rs.getString("Address"),
                rs.getString("City"),
                rs.getString("CuisineType"));
    }
}
