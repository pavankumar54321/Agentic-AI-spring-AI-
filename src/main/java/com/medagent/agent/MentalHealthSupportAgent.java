package com.medagent.agent;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.chat.client.ChatClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MentalHealthSupportAgent {
    private static final Logger log = LoggerFactory.getLogger(MentalHealthSupportAgent.class);

    private final ChatClient chatClient;

    public MentalHealthSupportAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Tool(description = "Detects emotional distress and provides calming support. Use this tool if the user indicates they are having a panic attack, feeling depressed, anxious, or overwhelmed.")
    public String provideMentalHealthSupport(String emotionalState) {
        log.info("Providing mental health support for state dynamically: {}", emotionalState);

        String prompt = "You are a specialized, empathetic mental health support agent. " +
                "The user has expressed the following emotional state: \"" + emotionalState + "\"\n" +
                "Provide a compassionate, validating, and calming response. " +
                "If the user indicates any form of self-harm or suicide, you MUST start your response with 'CRISIS DETECTED.' and urge them to call emergency services or a crisis hotline (like 988) immediately. " +
                "Keep the response concise and focused on immediate emotional support or grounding techniques.";

        try {
            return generateWithRetry(prompt);
        } catch (Exception e) {
            log.error("Failed to call LLM for mental health support after retries: ", e);
            return "Mental Health Support: I am here for you, but I'm currently experiencing technical difficulties. If you are in crisis, please call emergency services or a suicide prevention hotline immediately.";
        }
    }

    private String generateWithRetry(String prompt) throws Exception {
        int maxRetries = 3;
        long waitTime = 2000;
        Exception lastException = null;
        for (int i = 0; i < maxRetries; i++) {
            try {
                return chatClient.prompt(prompt).call().content();
            } catch (Exception e) {
                lastException = e;
                if (e.getMessage() != null && (e.getMessage().contains("503") || e.getMessage().contains("429"))) {
                    log.warn("High Demand/Rate Limit Error from Gemini. Retrying in {} ms (Attempt {}/{})", waitTime, i + 1, maxRetries);
                    try { Thread.sleep(waitTime); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    waitTime *= 2; // Exponential backoff
                } else {
                    throw e;
                }
            }
        }
        throw lastException;
    }
}
