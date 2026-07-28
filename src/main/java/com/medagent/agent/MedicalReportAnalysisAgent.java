package com.medagent.agent;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.chat.client.ChatClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MedicalReportAnalysisAgent {
    private static final Logger log = LoggerFactory.getLogger(MedicalReportAnalysisAgent.class);

    private final ChatClient chatClient;

    public MedicalReportAnalysisAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Tool(description = "Analyzes text-based medical reports, lab results, and prescriptions. Use this tool when the user provides specific medical values (e.g., glucose 110, BP 140/90) and asks for an explanation.")
    public String analyzeMedicalReport(String reportText) {
        log.info("Analyzing medical report text via LLM: {}", reportText);

        String prompt = "You are an expert medical lab technician. Analyze the following text extracted from a medical report or lab result: \"" + reportText + "\".\n" +
                "Identify any out-of-range values, explain what they mean in simple terms, and flag any critical warnings. " +
                "Always include a disclaimer that you are an AI and the patient should consult a doctor.";

        try {
            return chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            log.error("Failed to analyze medical report", e);
            return "Failed to process the report dynamically. Please consult a primary care physician regarding these lab results.";
        }
    }
}
