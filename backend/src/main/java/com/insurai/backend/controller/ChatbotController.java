package com.insurai.backend.controller;

import com.insurai.backend.service.GeminiChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @Autowired
    private GeminiChatbotService geminiChatbotService;

    @PostMapping("/message")
    public String getChatbotResponse(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        return geminiChatbotService.getResponse(userMessage);
    }
}
