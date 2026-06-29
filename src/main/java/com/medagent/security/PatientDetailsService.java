package com.medagent.security;

import com.medagent.domain.PatientProfile;
import com.medagent.repository.PatientProfileRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class PatientDetailsService implements UserDetailsService {

    private final PatientProfileRepository patientProfileRepository;

    public PatientDetailsService(PatientProfileRepository patientProfileRepository) {
        this.patientProfileRepository = patientProfileRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        PatientProfile patient = patientProfileRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new User(patient.getEmail(), patient.getPassword(), Collections.emptyList());
    }
}
