package dao;
import models.UserAddress;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserAddressDAO {





        private static final String TABLE = "UserAddress";

        public int create(UserAddress ua) throws SQLException {
            String sql = "INSERT INTO " + TABLE + " (Address, City) VALUES (?, ?)";
            try (Connection c = DataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, ua.getAddress());
                ps.setString(2, ua.getCity());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    return rs.next() ? rs.getInt(1) : -1;
                }
            }
        }

        public UserAddress findById(int addressId) throws SQLException {
            String sql = "SELECT AddressID, Address, City FROM " + TABLE + " WHERE AddressID = ?";
            try (Connection c = DataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setInt(1, addressId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            }
            return null;
        }

        public List<UserAddress> findAll() throws SQLException {
            List<UserAddress> list = new ArrayList<>();
            String sql = "SELECT AddressID, Address, City FROM " + TABLE + " ORDER BY AddressID DESC";
            try (Connection c = DataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) list.add(mapRow(rs));
            }
            return list;
        }

        public void update(UserAddress ua) throws SQLException {
            String sql = "UPDATE " + TABLE + " SET Address = ?, City = ? WHERE AddressID = ?";
            try (Connection c = DataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setString(1, ua.getAddress());
                ps.setString(2, ua.getCity());
                ps.setInt(3, ua.getAddressId());
                ps.executeUpdate();
            }
        }

        public void delete(int addressId) throws SQLException {
            String sql = "DELETE FROM " + TABLE + " WHERE AddressID = ?";
            try (Connection c = DataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setInt(1, addressId);
                ps.executeUpdate();
            }
        }

        private UserAddress mapRow(ResultSet rs) throws SQLException {
            return new UserAddress(
                    rs.getInt("AddressID"),
                    rs.getString("Address"),
                    rs.getString("City")
            );
        }
    }

