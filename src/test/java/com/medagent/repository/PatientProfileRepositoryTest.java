package com.medagent.repository;

import com.medagent.domain.PatientProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class PatientProfileRepositoryTest {

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @Test
    public void testSaveAndFindByEmail() {
        // Arrange
        PatientProfile profile = new PatientProfile();
        profile.setName("John Doe");
        profile.setAge(30);
        profile.setEmail("john.doe@example.com");
        profile.setPassword("hashedpassword123");
        profile.setParentGuardianNumber("+1234567890");

        // Act
        patientProfileRepository.save(profile);
        Optional<PatientProfile> retrievedProfile = patientProfileRepository.findByEmail("john.doe@example.com");

        // Assert
        assertThat(retrievedProfile).isPresent();
        assertThat(retrievedProfile.get().getName()).isEqualTo("John Doe");
        assertThat(retrievedProfile.get().getParentGuardianNumber()).isEqualTo("+1234567890");
    }
}
