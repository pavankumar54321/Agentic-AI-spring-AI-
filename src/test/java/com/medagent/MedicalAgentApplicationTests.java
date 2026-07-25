package com.medagent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MedicalAgentApplicationTests {

    @Test
    void contextLoads() {
        // This test simply verifies that the Spring Application Context loads successfully
        // without any fatal configuration errors.
    }

}
