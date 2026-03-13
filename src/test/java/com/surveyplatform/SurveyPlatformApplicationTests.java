package com.surveyplatform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Integration test verifying that the Spring application context loads successfully
@SpringBootTest
@ActiveProfiles("test")
class SurveyPlatformApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that all beans are properly configured and the context starts
    }
}
