package com.medagent.agent;

import org.springframework.ai.tool.annotation.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;

@Component
public class HospitalLocationAgent {
    private static final Logger log = LoggerFactory.getLogger(HospitalLocationAgent.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool(description = "Finds the nearest hospital or medical center. Use this when an emergency is detected or the user asks for a hospital recommendation. Provide the user's city or location.")
    public String findNearestHospital(String location) {
        log.info("Searching for hospitals near: {}", location);
        
        if (location == null || location.trim().isEmpty()) {
            return "Please provide your current city or location so I can find the nearest hospital.";
        }
        
        try {
            String url = "https://nominatim.openstreetmap.org/search?q=hospital+in+" + location.trim().replace(" ", "+") + "&format=json&limit=3";
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "MedicalAgentSystem/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray() && root.size() > 0) {
                    StringBuilder hospitals = new StringBuilder("Hospitals found near " + location + ":\n");
                    for (int i = 0; i < root.size(); i++) {
                        JsonNode node = root.get(i);
                        String name = node.has("name") ? node.get("name").asText() : "Hospital";
                        String displayName = node.has("display_name") ? node.get("display_name").asText() : "Unknown address";
                        hospitals.append("- ").append(name).append(" (Address: ").append(displayName).append(")\n");
                    }
                    hospitals.append("\nIf this is an emergency, it is highly recommended you proceed to the nearest one immediately or inform the paramedics.");
                    return hospitals.toString();
                }
            }
            return "Could not find specific hospitals in " + location + " dynamically. Please check local directories or call emergency services immediately if this is an emergency.";
        } catch (Exception e) {
            log.error("Error fetching hospital data: ", e);
            return "Failed to fetch real-time hospital data due to network error. If this is an emergency, call emergency services immediately.";
        }
    }
}
