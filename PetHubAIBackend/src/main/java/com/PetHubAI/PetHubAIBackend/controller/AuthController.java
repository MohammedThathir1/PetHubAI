package com.PetHubAI.PetHubAIBackend.controller;

import com.PetHubAI.PetHubAIBackend.dto.request.LoginRequest;
import com.PetHubAI.PetHubAIBackend.dto.request.SignupRequest;
import com.PetHubAI.PetHubAIBackend.dto.response.ApiResponse;
import com.PetHubAI.PetHubAIBackend.dto.response.AuthResponse;
import com.PetHubAI.PetHubAIBackend.dto.response.LoginResponse;
import com.PetHubAI.PetHubAIBackend.dto.response.UserResponse;
import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.service.AuthService;
import com.PetHubAI.PetHubAIBackend.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        AuthResponse response = authService.signup(signupRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Get user details from authentication
            User user = (User) authentication.getPrincipal();

            // Generate JWT token
            String token = jwtService.generateToken(user);

            // Create clean response without circular references
            LoginResponse loginResponse = new LoginResponse(token, user);

            return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid email or password"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }



//    @PostMapping("/login")
//    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
//        AuthResponse response = authService.login(loginRequest);
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        UserResponse response = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        // Since we're using stateless JWT, logout is handled on the client side
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}

