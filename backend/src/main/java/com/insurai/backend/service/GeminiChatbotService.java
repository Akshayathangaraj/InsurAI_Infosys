package com.insurai.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.insurai.backend.dto.AgentAvailabilityDTO;
import com.insurai.backend.entity.AgentPolicyMapping;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class GeminiChatbotService {

    private static final Logger log = LoggerFactory.getLogger(GeminiChatbotService.class);

    private final AgentAvailabilityService agentService;
    private final AgentPolicyMappingService mappingService;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    private final String apiKey = "AIzaSyBxH3fHAO95EX-YIwCrivQypXySjJaRJmQ";

    @Value("${gemini.api.url}")
    private String apiUrl;

    // Optional cached context to reduce DB hits (refresh every minute)
    private final AtomicReference<String> cachedContext = new AtomicReference<>("");
    private LocalDateTime lastContextFetchTime = null;

    public GeminiChatbotService(AgentAvailabilityService agentService,
                                AgentPolicyMappingService mappingService) {
        this.agentService = agentService;
        this.mappingService = mappingService;
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // ✅ Main chatbot response method
    public String getResponse(String userMessage) throws IOException {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Please enter a valid question.";
        }

        // Special case: user asks "What can you tell?"
        if (userMessage.trim().equalsIgnoreCase("what can you tell?")) {
            return buildCapabilitiesResponse();
        }

        // Get or refresh chatbot context
        String context = getCachedOrFetchContext();

        // Build request body for Gemini API
        String fullPrompt = buildPromptWithContext(context, userMessage);

        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contents = objectMapper.createArrayNode();
        ObjectNode content = objectMapper.createObjectNode();

        content.put("role", "user");
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
                .url(apiUrl)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Goog-Api-Key", apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                log.error("Gemini API error - HTTP {}: {}", response.code(), errorBody);
                throw new IOException("Gemini API error: " + response.code());
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            JsonNode jsonResponse = objectMapper.readTree(responseBody);

            // Safe extraction of chatbot reply
            JsonNode candidates = jsonResponse.path("candidates");
            if (candidates.isMissingNode() || !candidates.isArray() || candidates.isEmpty()) {
                log.warn("Gemini API returned no candidates: {}", responseBody);
                return "Sorry, I couldn't find a response. Please try again.";
            }

            JsonNode textNode = candidates.get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            if (textNode.isMissingNode()) {
                log.warn("No text found in Gemini response: {}", responseBody);
                return "I couldn't generate a response at the moment. Please try again later.";
            }

            return textNode.asText();
        } catch (IOException e) {
            log.error("Error while communicating with Gemini API", e);
            throw e;
        }
    }

    // ✅ Cached or live context builder
    private String getCachedOrFetchContext() {
        LocalDateTime now = LocalDateTime.now();
        if (lastContextFetchTime == null || lastContextFetchTime.plusMinutes(1).isBefore(now)) {
            log.info("Refreshing chatbot context from database...");
            String newContext = buildContextFromDatabase();
            cachedContext.set(newContext);
            lastContextFetchTime = now;
        }
        return cachedContext.get();
    }

    // ✅ Build chatbot context from database data
    private String buildContextFromDatabase() {
        StringBuilder context = new StringBuilder();

        try {
            List<AgentAvailabilityDTO> agents = agentService.getAllAgents();

            context.append("\n🧑‍💼 Agent Availability:\n")
                    .append("=================================\n");
            for (AgentAvailabilityDTO agent : agents) {
                context.append(String.format(
                        "Agent Name: %s | Available Date: %s\n",
                        agent.getName(),
                        agent.getDate()
                ));
            }
            context.append("\n🧑‍💼 All Agents:\n");
for (User agent : allAgents) {
    context.append(agent.getUsername()).append("\n");
}

context.append("\n👨‍💼 All Employees:\n");
for (Employee emp : allEmployees) {
    context.append(emp.getUser().getUsername())
           .append(" (").append(emp.getUser().getEmail()).append(")\n");
}

context.append("\n🛡️ All Admins:\n");
for (User admin : allAdmins) {
    context.append(admin.getUsername()).append("\n");
}

            context.append("\n📋 Agent-Policy Assignments:\n")
                    .append("=================================\n")
                    .append(mappingService.buildContextFromDatabase())
                    .append("\n");

            for (AgentAvailabilityDTO agent : agents) {
                Long agentId = agent.getId();
                List<AgentPolicyMapping> mappings = mappingService.getPoliciesByAgent(agentId);

                if (mappings.isEmpty()) {
                    context.append(String.format("Agent %s → No assigned policies.\n", agent.getName()));
                } else {
                    context.append(String.format("Agent: %s\n", agent.getName()));
                    for (AgentPolicyMapping mapping : mappings) {
                        context.append(String.format(
                                "   • Policy: %s | Type: %s | Coverage: %s | Premium: %s\n",
                                mapping.getPolicy().getPolicyName(),
                                mapping.getPolicy().getPolicyType(),
                                mapping.getPolicy().getCoverageAmount(),
                                mapping.getPolicy().getPremium()
                        ));
                    }
                    context.append("\n");
                }
            }
        } catch (Exception e) {
            log.error("Error building chatbot context from DB", e);
            context.append("\n⚠️ Error fetching context: ").append(e.getMessage()).append("\n");
        }

        return context.toString();
    }

    // ✅ Prompt formatting
    private String buildPromptWithContext(String context, String userMessage) {
        return "You are **InsurAI**, a smart insurance assistant chatbot.\n\n" +
                "Your knowledge base includes:\n" +
                "• Agent availability schedules\n" +
                "• Policies assigned to each agent\n" +
                "• Relevant database details\n\n" +
                "Use ONLY this data to answer. Respond clearly and professionally.\n\n" +
                "📚 DATABASE CONTEXT:\n" + context + "\n\n" +
                "💬 USER QUESTION: " + userMessage + "\n\n" +
                "Please provide a short, factual, and helpful answer.";
    }

    // ✅ Special response for "What can you tell?"
    private String buildCapabilitiesResponse() {
        return "I can provide information and answer questions based on the following database tables and data:\n\n" +
                "1️⃣ **Agents**: Names, roles, availability schedules.\n" +
                "2️⃣ **Policies**: Policy names, types, coverage amounts, premiums.\n" +
                "3️⃣ **Agent-Policy Mappings**: Which agents are assigned to which policies.\n" +
                "4️⃣ **Employees**: Employee details associated with claims.\n" +
                "5️⃣ **Claims**: Claim descriptions, amounts, status, assigned agent, decision info, documents.\n" +
                "6️⃣ **Claim Progress Notes**: Notes recorded by agents or admins regarding claim updates.\n\n" +
                "You can ask me questions like:\n" +
                "• Which agent is available on a specific date?\n" +
                "• Which policies are assigned to an agent?\n" +
                "• Status of a claim for a particular employee.\n" +
                "• Agent suggestions for a claim.\n" +
                "• Summary of employee claims statistics.\n\n" +
                "I use this information to answer your queries accurately and concisely.";
    }

    // ✅ Utility
    public String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
