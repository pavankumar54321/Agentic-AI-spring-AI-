package com.medagent.agent;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmergencyTriageAgent {
    private static final Logger log = LoggerFactory.getLogger(EmergencyTriageAgent.class);
    
    private final ChatLanguageModel chatLanguageModel;

    public EmergencyTriageAgent(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    @Tool("Detects medical emergencies and estimates urgency. Call this tool immediately if symptoms are provided.")
    public String evaluateEmergencyRisk(String symptoms) {
        log.info("Evaluating emergency risk dynamically for symptoms: {}", symptoms);
        
        String prompt = "You are a specialized medical triage agent. " +
                "Evaluate the following patient symptoms for emergency risk. " +
                "Symptoms: " + symptoms + "\n" +
                "Respond strictly with one of three severity levels (CRITICAL, MODERATE, or LOW) and provide a brief, actionable immediate guidance. " +
                "If it is CRITICAL, start your response with '⚠️ MEDICAL EMERGENCY DETECTED'. " +
                "Do not include conversational filler.";
                
        try {
            return generateWithRetry(prompt);
        } catch (Exception e) {
            log.error("Failed to call LLM for triage after retries: ", e);
            return "Emergency Risk: MODERATE. Unable to determine precise severity due to system error. Recommend consulting a doctor or emergency services if you feel it is an emergency.";
        }
    }

    private String generateWithRetry(String prompt) throws Exception {
        int maxRetries = 3;
        long waitTime = 2000;
        Exception lastException = null;
        for (int i = 0; i < maxRetries; i++) {
            try {
                return chatLanguageModel.generate(prompt);
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
