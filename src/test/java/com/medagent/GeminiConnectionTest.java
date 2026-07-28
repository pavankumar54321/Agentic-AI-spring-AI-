package com.medagent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("default") // to use the real api key from application.properties
class GeminiConnectionTest {
    @Autowired
    private ChatClient chatClient;

    @Test
    void testConnection() {
        System.out.println("TEST_START");
        try {
            String response = chatClient.prompt().user("Hello!").call().content();
            System.out.println("RESPONSE: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("TEST_END");
    }
}
