package com.medagent.agent;

import com.medagent.domain.PatientProfile;
import com.medagent.repository.PatientProfileRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MedicalMemoryAgent {
    private static final Logger log = LoggerFactory.getLogger(MedicalMemoryAgent.class);
    
    private final PatientProfileRepository repository;

    public MedicalMemoryAgent(PatientProfileRepository repository) {
        this.repository = repository;
    }

    @Tool(description = "Retrieves a patient's medical history, chronic conditions, and allergies by their name. Use this tool when you need to check if a patient has any pre-existing conditions or allergies.")
    public String getPatientHistory(String patientName) {
        log.info("Fetching history for patient: {}", patientName);
        Optional<PatientProfile> profile = repository.findByNameIgnoreCase(patientName);
        
        if (profile.isPresent()) {
            PatientProfile p = profile.get();
            return String.format("Patient Found: %s, Age: %d. Chronic Conditions: %s. Allergies: %s. Current Medications: %s.",
                    p.getName(), p.getAge(), 
                    String.join(", ", p.getChronicConditions()),
                    String.join(", ", p.getAllergies()),
                    String.join(", ", p.getCurrentMedications()));
        } else {
            return "No medical history found for patient: " + patientName + ". Proceed with caution and ask for allergies/medications if prescribing or recommending.";
        }
    }
}
