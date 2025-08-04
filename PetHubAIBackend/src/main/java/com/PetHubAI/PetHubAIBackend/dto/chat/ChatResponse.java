package com.PetHubAI.PetHubAIBackend.dto.chat;

import java.time.LocalDateTime;

public class ChatResponse {
    private String response;
    private String sessionId;
    private LocalDateTime timestamp;
    private boolean success;
    private String errorMessage;

    public ChatResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public static ChatResponse success(String response, String sessionId) {
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.response = response;
        chatResponse.sessionId = sessionId;
        chatResponse.success = true;
        return chatResponse;
    }

    public static ChatResponse error(String errorMessage) {
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.success = false;
        chatResponse.errorMessage = errorMessage;
        return chatResponse;
    }

    // Getters and setters
    public String getResponse() { return response; }
    public String getSessionId() { return sessionId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
}

