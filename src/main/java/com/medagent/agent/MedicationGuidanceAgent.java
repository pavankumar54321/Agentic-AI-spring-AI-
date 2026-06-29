package com.medagent.agent;

import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;

@Component
public class MedicationGuidanceAgent {
    private static final Logger log = LoggerFactory.getLogger(MedicationGuidanceAgent.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool("Provides guidance on medication usage, side effects, and precautions. Use this tool when a user asks about taking a specific non-prescription drug or wants to know side effects.")
    public String getMedicationGuidance(String medicationName, String patientAllergies) {
        log.info("Providing guidance for medication: {}, Allergies: {}", medicationName, patientAllergies);

        if (patientAllergies != null && patientAllergies.toLowerCase().contains(medicationName.toLowerCase())) {
            return "CRITICAL WARNING: Patient history indicates an allergy to " + medicationName + " or its components. DO NOT TAKE THIS MEDICATION. Consult a doctor immediately for an alternative.";
        }

        try {
            String url = "https://api.fda.gov/drug/label.json?search=openfda.brand_name:\"" + medicationName.trim().replace(" ", "+") + "\"&limit=1";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.has("results") && root.get("results").isArray() && root.get("results").size() > 0) {
                    JsonNode result = root.get("results").get(0);
                    
                    String warnings = result.has("warnings") ? result.get("warnings").get(0).asText() : "No specific warnings listed.";
                    String indications = result.has("indications_and_usage") ? result.get("indications_and_usage").get(0).asText() : "No specific indications listed.";
                    String doNotUse = result.has("do_not_use") ? result.get("do_not_use").get(0).asText() : "No specific do not use conditions listed.";
                    
                    return "Medication Guidance for " + medicationName + " (Source: FDA):\n" +
                           "- Indications & Usage: " + indications + "\n" +
                           "- Warnings: " + warnings + "\n" +
                           "- Do Not Use: " + doNotUse + "\n" +
                           "- Disclaimer: I cannot prescribe restricted drugs or provide definitive medical dosages. Always read the label and consult a doctor.";
                }
            }
            return "Could not find specific FDA data for " + medicationName + ". Please consult a pharmacist or doctor for specific dosage and interaction information. Remember to stay hydrated and read the label carefully.";
        } catch (Exception e) {
            log.error("Error fetching medication data: ", e);
            return "Failed to fetch real-time FDA medication data due to network error. Please consult a pharmacist or read the label carefully.";
        }
    }
}
