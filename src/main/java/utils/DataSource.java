package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {

    private static final String URL = System.getenv().getOrDefault("DB_URL",
            "jdbc:mysql://localhost:3306/food_ordering?useSSL=false&serverTimezone=UTC");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "BURAYA ŞİFRENİZİ GİRİN");

    public static Connection getConnection() throws SQLException {
        System.out.println("DB CONNECT TRY -> " + URL + " user=" + USER);

        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("DB CONNECT OK  -> " + conn.getMetaData().getURL());
            return conn;
        } catch (SQLException e) {
            System.out.println("DB CONNECT FAIL -> " + e.getMessage());
            throw e;
        }
    }
}
