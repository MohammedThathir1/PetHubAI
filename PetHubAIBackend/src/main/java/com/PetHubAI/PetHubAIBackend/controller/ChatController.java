package com.PetHubAI.PetHubAIBackend.controller;

import com.PetHubAI.PetHubAIBackend.dto.chat.ChatRequest;
import com.PetHubAI.PetHubAIBackend.dto.chat.ChatResponse;
import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.service.PetAssistantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {

    @Autowired
    private PetAssistantService petAssistantService;

    @PostMapping("/pet-assistant")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ChatResponse> chatWithPetAssistant(
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        String userId = user.getId().toString();

        ChatResponse response = petAssistantService.getChatResponse(request, userId);
        return ResponseEntity.ok(response);
    }
}
