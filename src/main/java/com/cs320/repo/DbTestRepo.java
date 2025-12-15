package com.cs320.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DbTestRepo {

    private final JdbcTemplate jdbc;

    public DbTestRepo(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String testConnection() {
        return jdbc.queryForObject("SELECT 1", String.class);
    }
}

