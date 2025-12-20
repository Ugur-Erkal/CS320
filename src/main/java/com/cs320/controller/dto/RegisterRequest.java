package com.cs320.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Username is required.")
    private String username;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, message = "Password must be at least 6 characters.")
    private String password;

    @NotBlank(message = "User type is required.")
    private String userType;

    @NotBlank(message = "Address is required.")
    private String address;

    @NotBlank(message = "Phone number is required.")
    private String phoneNumber;

    @NotBlank(message = "City is required.")
    private String city;

    public RegisterRequest() {
    }

    public RegisterRequest(String username, String password, String userType,
                          String address, String phoneNumber, String city) {
        this.username = username;
        this.password = password;
        this.userType = userType;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.city = city;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}

