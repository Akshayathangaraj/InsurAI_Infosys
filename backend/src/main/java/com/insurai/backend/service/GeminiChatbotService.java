package com.insurai.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.insurai.backend.dto.AgentAvailabilityDTO;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
@Service
public class GeminiChatbotService {

    private final AgentAvailabilityService agentService;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    // Hardcoded Gemini API key
    private final String apiKey = "AIzaSyBxH3fHAO95EX-YIwCrivQypXySjJaRJmQ";

    @Value("${gemini.api.url}")
    private String apiUrl;

    public GeminiChatbotService(AgentAvailabilityService agentService) {
        this.agentService = agentService;
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // ✅ Rename method to match controller
    public String getResponse(String userMessage) throws IOException {

        // Build context from database
        String context = buildContextFromDatabase();

        // Build full prompt
        String fullPrompt = buildPromptWithContext(context, userMessage);

        // Build request body for Gemini API
        ObjectNode requestBody = objectMapper.createObjectNode();

        ArrayNode contents = objectMapper.createArrayNode();
        ObjectNode content = objectMapper.createObjectNode();

        content.put("role", "user"); // required by Gemini API

        ArrayNode parts = objectMapper.createArrayNode();
        ObjectNode part = objectMapper.createObjectNode();
        part.put("text", fullPrompt);
        parts.add(part);

        content.set("parts", parts);
        contents.add(content);
        requestBody.set("contents", contents);

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(apiUrl) // API URL from application.properties
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Goog-Api-Key", apiKey)
                .post(body)
                .build();

        // Execute request
        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                String errorBody = "";
                try (ResponseBody responseBody = response.body()) {
                    if (responseBody != null) errorBody = responseBody.string();
                }
                System.err.println("--- Gemini API Error Response ---");
                System.err.println("HTTP Status: " + response.code());
                System.err.println("Error Body: " + errorBody);
                System.err.println("---------------------------------");
                throw new IOException("Unexpected response code: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            JsonNode jsonResponse = objectMapper.readTree(responseBody);

            // Extract text from Gemini response
            return jsonResponse
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        }
    }

    private String buildContextFromDatabase() {
        List<AgentAvailabilityDTO> agents = agentService.getAllAgents();
        StringBuilder context = new StringBuilder();
        context.append("\n\nAvailable Agents in Our Database:\n");
        context.append("=================================\n");

        for (AgentAvailabilityDTO agent : agents) {
            context.append(String.format(
                    "\nAgent Name: %s\n" +
                    "Available Date: %s\n" +
                    "---\n",
                    agent.getName(),
                    agent.getDate()
            ));
        }
        return context.toString();
    }

    private String buildPromptWithContext(String context, String userMessage) {
        return "You are InsurAI, an intelligent assistant for agent availability.\n\n" +
                "Guidelines:\n" +
                "1. Be professional and helpful.\n" +
                "2. Provide answers ONLY based on the agent availability listed below.\n" +
                "3. Keep responses concise.\n\n" +
                context + "\n\n" +
                "USER QUESTION: " + userMessage + "\n\n" +
                "Please provide a helpful, accurate response based on the information above:";
    }

    public String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
