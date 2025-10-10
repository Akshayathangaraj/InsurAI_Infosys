package com.insurai.backend.controller;

import com.insurai.backend.service.GeminiChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @Autowired
    private GeminiChatbotService geminiChatbotService;

    @PostMapping("/message")
    public String getChatbotResponse(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");

        try {
            // Call the service and return the response
            return geminiChatbotService.getResponse(userMessage);
        } catch (IOException e) {
            // Handle exception gracefully
            e.printStackTrace();
            return "Sorry, we couldn't process your request at the moment. Error: " + e.getMessage();
        }
    }
}
