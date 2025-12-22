package models;

public class user {

    private int userId;
    private String username;
    private String password;
    private String userType; // "Customer" veya "Manager"

    public user(int userId, String username, String password, String userType) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.userType = userType;
    }

    public user(String username, String password, String userType) {
        this.username = username;
        this.password = password;
        this.userType = userType;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getUserType() {
        return userType;
    }
}