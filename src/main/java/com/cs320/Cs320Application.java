package com.cs320;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Cs320Application {

    public static void main(String[] args) {
        SpringApplication.run(Cs320Application.class, args);
    }

    @Bean
    CommandLineRunner run() {
        return args -> {
            System.out.println("=======================================");
            System.out.println(" Application started");
            System.out.println("http://localhost:8080/");
            System.out.println("=======================================");
        };
    }
}
