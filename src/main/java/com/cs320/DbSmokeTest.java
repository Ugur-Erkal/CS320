package com.cs320;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DbSmokeTest implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    public DbSmokeTest(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        try {
            Integer one = jdbc.queryForObject("SELECT 1", Integer.class);
            System.out.println("✅ DB OK: SELECT 1 -> " + one);

            // DB adını da görelim
            String db = jdbc.queryForObject("SELECT DATABASE()", String.class);
            System.out.println("✅ Connected DB -> " + db);

        } catch (Exception e) {
            System.out.println("❌ DB FAIL: " + e.getClass().getSimpleName() + " -> " + e.getMessage());
        }
    }
}
