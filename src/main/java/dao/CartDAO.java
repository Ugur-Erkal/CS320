package dao;
import models.Cart;
import utils.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {




        private static final String CART_TABLE = "Cart";

        public int create(String status) throws SQLException {
            String sql = "INSERT INTO " + CART_TABLE + " (Status, AcceptedAt) VALUES (?, NULL)";

            try (Connection c = DataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, status);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    return rs.next() ? rs.getInt(1) : -1;
                }
            }
        }

        public Cart findById(int cartId) throws SQLException {
            String sql = "SELECT CartID, Status, AcceptedAt FROM " + CART_TABLE + " WHERE CartID = ?";

            try (Connection c = DataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setInt(1, cartId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            }
            return null;
        }

        public List<Cart> findAll() throws SQLException {
            List<Cart> list = new ArrayList<>();
            String sql = "SELECT CartID, Status, AcceptedAt FROM " + CART_TABLE + " ORDER BY CartID DESC";

            try (Connection c = DataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) list.add(mapRow(rs));
            }
            return list;
        }

        public void updateStatus(int cartId, String newStatus) throws SQLException {
            String sql = "UPDATE " + CART_TABLE + " SET Status = ? WHERE CartID = ?";

            try (Connection c = DataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setString(1, newStatus);
                ps.setInt(2, cartId);
                ps.executeUpdate();
            }
        }

        public void setAcceptedAtNow(int cartId) throws SQLException {
            String sql = "UPDATE " + CART_TABLE + " SET AcceptedAt = CURRENT_TIMESTAMP WHERE CartID = ?";

            try (Connection c = DataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setInt(1, cartId);
                ps.executeUpdate();
            }
        }

        public void delete(int cartId) throws SQLException {
            String sql = "DELETE FROM " + CART_TABLE + " WHERE CartID = ?";

            try (Connection c = DataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setInt(1, cartId);
                ps.executeUpdate();
            }
        }

        private Cart mapRow(ResultSet rs) throws SQLException {
            return new Cart(
                    rs.getInt("CartID"),
                    rs.getString("Status"),
                    rs.getTimestamp("AcceptedAt")
            );
        }
    }

