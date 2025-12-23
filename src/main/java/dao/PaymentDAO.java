package dao;

import models.payment;
import utils.DataSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    private static final String PAYMENT_TABLE = "Payment";

    public void create(int userId, BigDecimal amount) throws SQLException {
        String sql = "INSERT INTO " + PAYMENT_TABLE +
                " (UserID, Amount, PaidAt) VALUES (?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setBigDecimal(2, amount);

            ps.executeUpdate();
        }
    }

    public payment findById(int paymentId) throws SQLException {
        String sql = "SELECT PaymentID, UserID, Amount, PaidAt FROM " +
                PAYMENT_TABLE + " WHERE PaymentID = ?";

        try (Connection conn = DataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, paymentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<models.payment> findByUser(int userId) throws SQLException {
        List<models.payment> payments = new ArrayList<>();

        String sql = "SELECT PaymentID, UserID, Amount, PaidAt FROM " +
                PAYMENT_TABLE + " WHERE UserID = ? ORDER BY PaidAt DESC";

        try (Connection conn = DataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapRow(rs));
                }
            }
        }
        return payments;
    }

    public List<payment> findAll() throws SQLException {
        List<payment> payments = new ArrayList<>();

        String sql = "SELECT PaymentID, UserID, Amount, PaidAt FROM " +
                PAYMENT_TABLE + " ORDER BY PaidAt DESC";

        try (Connection conn = DataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                payments.add(mapRow(rs));
            }
        }
        return payments;
    }

    private payment mapRow(ResultSet rs) throws SQLException {
        return new payment(
                rs.getInt("PaymentID"),
                rs.getInt("UserID"),
                rs.getBigDecimal("Amount"),
                rs.getTimestamp("PaidAt"));
    }
}
