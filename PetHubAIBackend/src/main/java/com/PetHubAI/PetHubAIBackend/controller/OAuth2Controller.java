package com.PetHubAI.PetHubAIBackend.controller;

import com.PetHubAI.PetHubAIBackend.dto.response.AuthResponse;
import com.PetHubAI.PetHubAIBackend.dto.response.UserResponse;
import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.service.JwtService;
import com.PetHubAI.PetHubAIBackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oauth2")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OAuth2Controller {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/success")
    public ResponseEntity<AuthResponse> getOAuth2User(@AuthenticationPrincipal OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        User user = userService.findByEmail(email);

        String token = jwtService.generateToken(user);
        UserResponse userResponse = new UserResponse(user);

        return ResponseEntity.ok(new AuthResponse(token, userResponse));
    }
}

