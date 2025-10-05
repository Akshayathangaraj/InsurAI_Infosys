package com.insurai.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Use the test profile
class BackendApplicationTests {

    @Test
    void contextLoads() {
        // This test will load the Spring context using H2 in-memory DB
    }

}
