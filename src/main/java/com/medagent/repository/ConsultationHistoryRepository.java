package com.medagent.repository;

import com.medagent.domain.ConsultationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultationHistoryRepository extends JpaRepository<ConsultationHistory, Long> {
    List<ConsultationHistory> findByPatientId(Long patientId);
}
