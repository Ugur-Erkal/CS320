package com.cs320.controller;

import com.cs320.repo.DbTestRepo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DbTestController {

    private final DbTestRepo repo;

    public DbTestController(DbTestRepo repo) {
        this.repo = repo;
    }

    @GetMapping("/db-test")
    public String test() {
        try {
            return "DB OK: " + repo.testConnection();
        } catch (Exception e) {
            return "DB ERROR: " + e.getClass().getName() + " | " + e.getMessage();
        }
    }
}
