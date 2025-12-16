package com.cs320.controller.dto;

public class UserLoginResult {
    private final int userId;
    private final String userType;

    public UserLoginResult(int userId, String userType) {
        this.userId = userId;
        this.userType = userType;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserType() {
        return userType;
    }
}
