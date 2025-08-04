package com.PetHubAI.PetHubAIBackend.controller;

import com.PetHubAI.PetHubAIBackend.dto.response.ApiResponse;
import com.PetHubAI.PetHubAIBackend.dto.response.UserResponse;
import com.PetHubAI.PetHubAIBackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserProfile(Authentication authentication) {
        UserResponse response = userService.getUserProfile(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> getUserDashboard(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Welcome to your dashboard!"));
    }
}

