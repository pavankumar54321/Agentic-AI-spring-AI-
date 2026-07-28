package com.medagent.agent;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.chat.client.ChatClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SymptomAnalysisAgent {
    private static final Logger log = LoggerFactory.getLogger(SymptomAnalysisAgent.class);
    
    private final ChatClient chatClient;

    public SymptomAnalysisAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Tool(description = "Extracts structured medical symptoms, severity, and duration from the user's raw description. Call this tool when you need to understand the patient's symptoms clearly.")
    public String analyzeSymptoms(String patientDescription) {
        log.info("Analyzing symptoms dynamically for: {}", patientDescription);
        
        String prompt = "You are a clinical symptom extractor. Analyze the following patient description: \"" + patientDescription + "\".\n" +
                "Extract and strictly format the response as a JSON-like list detailing:\n" +
                "- Primary Symptoms:\n" +
                "- Estimated Severity (Low/Moderate/High):\n" +
                "- Duration:\n" +
                "- Missing Information (what follow-up questions should the doctor ask?):\n" +
                "Do not include conversational text, only the structured extraction.";
                
        try {
            return chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            log.error("Failed to extract symptoms", e);
            return "Symptom Analysis Complete (Fallback). Ensure you ask for Primary Symptoms, Severity, and Duration.";
        }
    }
}
