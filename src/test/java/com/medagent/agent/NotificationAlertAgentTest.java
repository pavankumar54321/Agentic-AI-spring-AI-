package com.medagent.agent;
import com.medagent.domain.PatientProfile;
import com.medagent.repository.PatientProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
public class NotificationAlertAgentTest {
    @Mock
    private PatientProfileRepository patientProfileRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private NotificationAlertAgent notificationAlertAgent;
    @BeforeEach
    public void setup() {
        // Set mock properties that are usually injected by Spring
        ReflectionTestUtils.setField(notificationAlertAgent, "accountSid", "mockSid");
        ReflectionTestUtils.setField(notificationAlertAgent, "authToken", "mockToken");
        ReflectionTestUtils.setField(notificationAlertAgent, "fromNumber", "+1234567890");

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testSendEmergencySOS_SuccessPath_CatchesTwilioException() {
        // Arrange
        String email = "test@test.com";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);

        PatientProfile profile = new PatientProfile();
        profile.setName("Test User");
        profile.setParentGuardianNumber("+0987654321");
        
        when(patientProfileRepository.findByEmail(email)).thenReturn(Optional.of(profile));

        // Act
        String result = notificationAlertAgent.sendEmergencySOS("123 Main St", "Heart Attack");

        // Assert
        // Since we are using mock Twilio credentials, Message.creator().create() will throw an exception.
        // We assert that the agent correctly catches it and returns the fallback message.
        assertThat(result).contains("Failed to dispatch SOS alert due to SMS gateway error");
        assertThat(result).contains("Advise user to manually call their emergency contact");
    }

    @Test
    public void testSendEmergencySOS_NoProfileFound() {
        // Arrange
        String email = "unknown@test.com";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        
        when(patientProfileRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        String result = notificationAlertAgent.sendEmergencySOS("123 Main St", "Heart Attack");

        // Assert
        assertThat(result).isEqualTo("Failed to send SOS: Could not locate user profile for authentication principal.");
    }
}
