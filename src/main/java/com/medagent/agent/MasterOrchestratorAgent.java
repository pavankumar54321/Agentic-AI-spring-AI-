package com.medagent.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class MasterOrchestratorAgent {

    private final ChatClient chatClient;
    private final EmergencyTriageAgent emergencyTriageAgent;
    private final HospitalLocationAgent hospitalLocationAgent;
    private final MedicalMemoryAgent medicalMemoryAgent;
    private final MedicalReportAnalysisAgent medicalReportAnalysisAgent;
    private final MedicationGuidanceAgent medicationGuidanceAgent;
    private final MentalHealthSupportAgent mentalHealthSupportAgent;
    private final MultilingualTranslationAgent multilingualTranslationAgent;
    private final NotificationAlertAgent notificationAlertAgent;
    private final PreventiveHealthcareAgent preventiveHealthcareAgent;
    private final RagMedicalKnowledgeAgent ragMedicalKnowledgeAgent;
    private final SymptomAnalysisAgent symptomAnalysisAgent;

    public MasterOrchestratorAgent(
            ChatClient chatClient,
            EmergencyTriageAgent emergencyTriageAgent,
            HospitalLocationAgent hospitalLocationAgent,
            MedicalMemoryAgent medicalMemoryAgent,
            MedicalReportAnalysisAgent medicalReportAnalysisAgent,
            MedicationGuidanceAgent medicationGuidanceAgent,
            MentalHealthSupportAgent mentalHealthSupportAgent,
            MultilingualTranslationAgent multilingualTranslationAgent,
            NotificationAlertAgent notificationAlertAgent,
            PreventiveHealthcareAgent preventiveHealthcareAgent,
            RagMedicalKnowledgeAgent ragMedicalKnowledgeAgent,
            SymptomAnalysisAgent symptomAnalysisAgent) {
        
        this.chatClient = chatClient;
        this.emergencyTriageAgent = emergencyTriageAgent;
        this.hospitalLocationAgent = hospitalLocationAgent;
        this.medicalMemoryAgent = medicalMemoryAgent;
        this.medicalReportAnalysisAgent = medicalReportAnalysisAgent;
        this.medicationGuidanceAgent = medicationGuidanceAgent;
        this.mentalHealthSupportAgent = mentalHealthSupportAgent;
        this.multilingualTranslationAgent = multilingualTranslationAgent;
        this.notificationAlertAgent = notificationAlertAgent;
        this.preventiveHealthcareAgent = preventiveHealthcareAgent;
        this.ragMedicalKnowledgeAgent = ragMedicalKnowledgeAgent;
        this.symptomAnalysisAgent = symptomAnalysisAgent;
    }

    private static final String SYSTEM_PROMPT = """
            You are the Master Orchestrator Agent of an Autonomous Medical Emergency Multi-Agent System.
            Your role is to coordinate specialized agents (Symptom Analysis, Emergency Triage, Medical Memory, etc.) to assist the user.
            
            CRITICAL RULE 1: If the user requests the response in a specific language (e.g., Hindi, Telugu), you MUST output your ENTIRE response in that target language. First, call the MultilingualTranslationAgent to get formatting guidelines.
            CRITICAL RULE 2: You MUST ALWAYS output your response in the strictly required 7-section format below, even if you are just asking clarifying questions. DO NOT output conversational questions without this structure.
            
            Always prioritize:
            1. Human safety
            2. Emergency detection
            3. Accuracy
            4. Clarity
            
            Use the provided tools to extract symptoms, check medical history, and evaluate emergency risk.
            If an emergency is detected, you MUST begin your response with "⚠️ MEDICAL EMERGENCY DETECTED" and do not delay.
            If the user explicitly asks for a hospital or medical center, you MUST call the HospitalLocationAgent immediately to provide that information in your response, even if you still need to ask follow-up questions about their symptoms.
            
            Always structure your final output clearly into exactly these 7 sections (translate these headers if the user requested a different language):
            1. Symptom Summary
            2. Severity Assessment
            3. Immediate Guidance
            4. Recommended Next Steps
            5. Emergency Recommendation (if needed)
            6. Preventive Advice
            7. Follow-up Questions
            
            Do NOT answer directly without reasoning and using tools when appropriate.
            Be calm, professional, fast, structured, and empathetic.
            """;

    public String processMedicalQuery(String query) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(query)
                .tools(
                        emergencyTriageAgent,
                        hospitalLocationAgent,
                        medicalMemoryAgent,
                        medicalReportAnalysisAgent,
                        medicationGuidanceAgent,
                        mentalHealthSupportAgent,
                        multilingualTranslationAgent,
                        notificationAlertAgent,
                        preventiveHealthcareAgent,
                        ragMedicalKnowledgeAgent,
                        symptomAnalysisAgent
                )
                .call()
                .content();
    }
}
