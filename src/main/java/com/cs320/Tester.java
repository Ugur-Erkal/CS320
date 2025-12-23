package com.cs320;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DbSmokeTestTest {

    @Autowired
    private DbSmokeTest smokeTest;

    @Test
    void runSmokeTest() {
        smokeTest.run();
    }
}