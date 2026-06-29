package com.medagent.agent;

import dev.langchain4j.agent.tool.Tool;
import com.medagent.domain.PatientProfile;
import com.medagent.repository.PatientProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import java.util.Optional;

@Component
public class NotificationAlertAgent {
    private static final Logger log = LoggerFactory.getLogger(NotificationAlertAgent.class);

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    private final PatientProfileRepository patientProfileRepository;

    public NotificationAlertAgent(PatientProfileRepository patientProfileRepository) {
        this.patientProfileRepository = patientProfileRepository;
    }

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    @Tool("Dispatches an emergency SOS alert to the user's parent/guardian. Use this tool ONLY when an emergency is CRITICAL. Do NOT ask the user for a phone number or name, the system automatically retrieves it from their secure profile.")
    public String sendEmergencySOS(String location, String criticalCondition) {
        log.info("DISPATCHING SOS for Location {}, Condition {}", location, criticalCondition);
        
        // 1. Get currently authenticated user's email
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // 2. Retrieve Patient Profile
        Optional<PatientProfile> profileOpt = patientProfileRepository.findByEmail(email);
        
        if (profileOpt.isEmpty()) {
             return "Failed to send SOS: Could not locate user profile for authentication principal.";
        }
        
        PatientProfile profile = profileOpt.get();
        String emergencyContactPhone = profile.getParentGuardianNumber();
        String patientName = profile.getName();

        if (emergencyContactPhone == null || emergencyContactPhone.trim().isEmpty()) {
            return "Failed to send SOS: No parent/guardian phone number found in the user's secure profile.";
        }

        try {
            String smsBody = "URGENT SOS ALERT from Aura AI: " + 
                             (patientName != null && !patientName.isEmpty() ? patientName : "A family member") + 
                             " is experiencing a critical medical emergency (" + criticalCondition + ") near " + 
                             (location != null && !location.isEmpty() ? location : "their current location") + 
                             ". Please check on them or dispatch emergency services immediately.";

            Message message = Message.creator(
                    new PhoneNumber(emergencyContactPhone.trim()),
                    new PhoneNumber(fromNumber),
                    smsBody
            ).create();

            return "SOS ALERT DISPATCHED SUCCESSFULLY. " +
                   "Family member at " + emergencyContactPhone + " has been notified via SMS (Message SID: " + message.getSid() + ") of the critical " + criticalCondition + " situation.";
        } catch (Exception e) {
            log.error("Failed to send Twilio SMS", e);
            return "Failed to dispatch SOS alert due to SMS gateway error: " + e.getMessage() + ". Advise user to manually call their emergency contact immediately.";
        }
    }
}
