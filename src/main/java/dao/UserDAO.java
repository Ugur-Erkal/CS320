package dao;
import models.User;
import utils.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


    public class UserDAO {

        private static final String USER_TABLE = "`User`";


        public void addUser(User user) throws SQLException {
            String sql = "INSERT INTO " + USER_TABLE +
                    " (Username, Password, UserType) VALUES (?, ?, ?)";

            try (Connection conn = DataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getUserType());

                ps.executeUpdate();
            }
        }


        public User findById(int userId) throws SQLException {
            String sql = "SELECT UserID, Username, Password, UserType FROM " +
                    USER_TABLE + " WHERE UserID = ?";

            try (Connection conn = DataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapRow(rs);
                    }
                }
            }
            return null;
        }


        public User findByUsername(String username) throws SQLException {
            String sql = "SELECT UserID, Username, Password, UserType FROM " +
                    USER_TABLE + " WHERE Username = ?";

            try (Connection conn = DataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapRow(rs);
                    }
                }
            }
            return null;
        }

        /* ======================
           LOGIN (şimdilik plain)
           ====================== */
        public User login(String username, String password) throws SQLException {
            String sql = "SELECT UserID, Username, Password, UserType FROM " +
                    USER_TABLE + " WHERE Username = ? AND Password = ?";

            try (Connection conn = DataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, username);
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapRow(rs);
                    }
                }
            }
            return null;
        }


        public List<User> findAll() throws SQLException {
            List<User> users = new ArrayList<>();
            String sql = "SELECT UserID, Username, Password, UserType FROM " +
                    USER_TABLE + " ORDER BY Username";

            try (Connection conn = DataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
            return users;
        }


        public void updateUser(User user) throws SQLException {
            String sql = "UPDATE " + USER_TABLE +
                    " SET Username = ?, Password = ?, UserType = ?" +
                    " WHERE UserID = ?";

            try (Connection conn = DataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getUserType());
                ps.setInt(4, user.getUserId());

                ps.executeUpdate();
            }
        }


        public void deleteUser(int userId) throws SQLException {
            String sql = "DELETE FROM " + USER_TABLE + " WHERE UserID = ?";

            try (Connection conn = DataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, userId);
                ps.executeUpdate();
            }
        }


        private User mapRow(ResultSet rs) throws SQLException {
            return new User(
                    rs.getInt("UserID"),
                    rs.getString("Username"),
                    rs.getString("Password"),
                    rs.getString("UserType")
            );
        }
    }


