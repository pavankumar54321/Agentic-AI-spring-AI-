package com.medagent.agent;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.chat.client.ChatClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PreventiveHealthcareAgent {
    private static final Logger log = LoggerFactory.getLogger(PreventiveHealthcareAgent.class);

    private final ChatClient chatClient;

    public PreventiveHealthcareAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Tool(description = "Provides long-term wellness guidance, nutrition advice, and preventive strategies for chronic diseases. Use this tool when a user asks about diet, exercise, or preventing hereditary conditions.")
    public String providePreventiveAdvice(String healthTopic) {
        log.info("Providing preventive healthcare advice dynamically for topic: {}", healthTopic);

        String prompt = "You are a specialized preventive healthcare agent. " +
                "Provide evidence-based, concise, and actionable preventive healthcare advice regarding the following topic: \"" + healthTopic + "\"\n" +
                "Include general guidelines on diet, lifestyle, and regular check-ups if applicable. " +
                "Do not prescribe medications. Keep the response informative but easy to read.";

        try {
            return generateWithRetry(prompt);
        } catch (Exception e) {
            log.error("Failed to call LLM for preventive healthcare after retries: ", e);
            return "Preventive Healthcare: A holistic approach to wellness includes sleeping 7-9 hours a night, staying properly hydrated, " +
                   "eating a diverse range of whole foods, and engaging in regular physical activity. Consult a doctor for more personalized advice.";
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
