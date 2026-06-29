package com.medagent.repository;

import com.medagent.domain.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {
    Optional<PatientProfile> findByNameIgnoreCase(String name);
    Optional<PatientProfile> findByEmail(String email);
}
