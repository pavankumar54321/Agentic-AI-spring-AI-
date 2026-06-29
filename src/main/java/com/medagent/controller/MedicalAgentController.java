package com.medagent.controller;

import com.medagent.agent.MasterOrchestratorAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/medical")
@CrossOrigin(origins = "*")
public class MedicalAgentController {

    private static final Logger log = LoggerFactory.getLogger(MedicalAgentController.class);
    private final MasterOrchestratorAgent orchestratorAgent;

    public MedicalAgentController(MasterOrchestratorAgent orchestratorAgent) {
        this.orchestratorAgent = orchestratorAgent;
    }

    @PostMapping("/query")
    public ResponseEntity<Map<String, String>> handleQuery(@RequestBody Map<String, String> request) {
        String userQuery = request.get("query");
        if (userQuery == null || userQuery.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Query cannot be empty"));
        }

        log.info("Received query: {}", userQuery);
        
        try {
            String response = orchestratorAgent.processMedicalQuery(userQuery);
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            log.error("Error processing medical query", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }
}
