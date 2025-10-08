package com.insurai.backend.service;

import com.insurai.backend.dto.AgentAvailabilityDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeminiChatbotService {

    @Value("${gemini.service-account-json}")
    private String serviceAccountJsonPath;

    private final RestTemplate restTemplate;
    private final AgentAvailabilityService agentService;

    @Autowired
    public GeminiChatbotService(AgentAvailabilityService agentService, RestTemplate restTemplate) {
        this.agentService = agentService;
        this.restTemplate = restTemplate;
    }

    public String getResponse(String userMessage) {
        try {
            // 1️⃣ Prepare agent info for prompt
            List<AgentAvailabilityDTO> agents = agentService.getAllAgents();
            String agentInfo = agents.stream()
                    .filter(a -> a != null && a.getName() != null && a.getDate() != null)
                    .map(a -> "Agent " + a.getName() + " is available on " + a.getDate())
                    .collect(Collectors.joining("\n"));

            String fullPrompt = "You are InsurAI, an intelligent assistant.\n\n"
                    + (agentInfo.isEmpty() ? "No agent availability found.\n" : agentInfo + "\n\n")
                    + "User: " + (userMessage != null ? userMessage : "");

            // 2️⃣ Build request body
            Map<String, Object> requestBody = Map.of(
                    "model", "gemini-1.5-flash",
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", fullPrompt)
                            ))
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.7,
                            "maxOutputTokens", 500
                    )
            );

            // 3️⃣ Get access token from service account
            String accessToken = getAccessToken();

            // 4️⃣ Setup headers with Bearer token
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            String url = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent";


            // 5️⃣ Make API call
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            // 6️⃣ Parse response
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    if (content != null) {
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            Object text = parts.get(0).get("text");
                            if (text != null) return text.toString();
                        }
                    }
                }
            } else {
                System.err.println("Gemini API failed with status: " + response.getStatusCode());
                System.err.println("Response body: " + response.getBody());
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error while calling Gemini API: " + e.getMessage());
        }

        return "Sorry, I couldn't process your request at the moment.";
    }

    private String getAccessToken() throws Exception {
        try (InputStream serviceAccountStream =
                     new ClassPathResource(serviceAccountJsonPath.replace("classpath:", "")).getInputStream()) {

            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(serviceAccountStream)
                    .createScoped(List.of("https://www.googleapis.com/auth/generative-language"));

            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        }
    }
}
