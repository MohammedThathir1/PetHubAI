package com.PetHubAI.PetHubAIBackend.service;

import com.PetHubAI.PetHubAIBackend.dto.chat.ChatRequest;
import com.PetHubAI.PetHubAIBackend.dto.chat.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PetAssistantService {

    @Autowired
    private GeminiApiService geminiApiService;

    public ChatResponse getChatResponse(ChatRequest request, String userId) {
        try {
            String sessionId = request.getSessionId() != null ?
                    request.getSessionId() : "chat_" + userId + "_" + System.currentTimeMillis();

            String enhancedPrompt = buildPetCarePrompt(request.getMessage());
            String aiResponse = geminiApiService.generateResponse(enhancedPrompt);

            return ChatResponse.success(aiResponse, sessionId);

        } catch (Exception e) {
            return ChatResponse.error("I'm having trouble right now. Please try again! 🐾");
        }
    }

    private String buildPetCarePrompt(String userMessage) {
        return """
            You are a friendly AI Pet Care Assistant. Help with:
            - Pet health and wellness
            - Training and behavior
            - Nutrition advice
            - General pet care
            
            Be helpful, friendly, and concise (200-300 words max).
            For serious health issues, recommend consulting a vet.
            
            User question: """ + userMessage;
    }
}

