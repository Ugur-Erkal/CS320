package com.cs320.service;

import com.cs320.controller.dto.UserLoginResult;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {

    private final JdbcTemplate jdbc;

    public UserService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void register(String username,
            String password,
            String userType,
            String address,
            String phoneNumber,
            String city) {

        try {
            Integer userId = insertUser(username, password, userType);

            Integer addressId = insertAddress(address, city);

            jdbc.update("INSERT INTO Lives (UserID, AddressID) VALUES (?, ?)",
                    userId, addressId);

            Integer phoneId = insertPhone(phoneNumber);

            jdbc.update("INSERT INTO HasPhoneNum (UserID, PhoneID) VALUES (?, ?)",
                    userId, phoneId);

        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("Username or phone number already exists.");
        }
    }

    private Integer insertUser(String username, String password, String userType) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO User (Username, Password, UserType) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, userType);
            return ps;
        }, kh);

        return Objects.requireNonNull(kh.getKey(), "Failed to generate user ID").intValue();
    }

    private Integer insertAddress(String address, String city) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO UserAddress (Address, City) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, address);
            ps.setString(2, city);
            return ps;
        }, kh);

        return Objects.requireNonNull(kh.getKey(), "Failed to generate address ID").intValue();
    }

    private Integer insertPhone(String phoneNumber) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO UserPhoneNumber (PhoneNumber) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, phoneNumber);
            return ps;
        }, kh);

        return Objects.requireNonNull(kh.getKey(), "Failed to generate phone ID").intValue();
    }

    public Optional<UserLoginResult> login(String username, String password) {
        return jdbc.query(
                "SELECT UserID, UserType FROM `User` WHERE Username = ? AND Password = ?",
                rs -> {
                    if (rs.next()) {
                        return Optional.of(new UserLoginResult(
                                rs.getInt("UserID"),
                                rs.getString("UserType")));
                    }
                    return Optional.empty();
                },
                username, password);
    }

    public Optional<String> getUserCity(int userId) {
        return jdbc.query(
                """
                        SELECT ua.City
                        FROM Lives l
                        JOIN UserAddress ua ON ua.AddressID = l.AddressID
                        WHERE l.UserID = ?
                        LIMIT 1
                        """,
                rs -> rs.next() ? Optional.ofNullable(rs.getString("City")) : Optional.empty(),
                userId);
    }

    public java.util.Optional<String> getUserType(int userId) {
        return jdbc.query(
                "SELECT UserType FROM User WHERE UserID = ?",
                rs -> rs.next() ? java.util.Optional.ofNullable(rs.getString("UserType")) : java.util.Optional.empty(),
                userId);
    }

}
