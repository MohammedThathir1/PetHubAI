package com.PetHubAI.PetHubAIBackend.service;

import com.PetHubAI.PetHubAIBackend.dto.request.LoginRequest;
import com.PetHubAI.PetHubAIBackend.dto.request.SignupRequest;
import com.PetHubAI.PetHubAIBackend.dto.response.AuthResponse;
import com.PetHubAI.PetHubAIBackend.dto.response.UserResponse;
import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.entity.UserProfile;
import com.PetHubAI.PetHubAIBackend.exception.AuthenticationException;
import com.PetHubAI.PetHubAIBackend.repository.UserProfileRepository;
import com.PetHubAI.PetHubAIBackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        // Check if user already exists
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new AuthenticationException("Email already registered");
            }

            // Validate password strength
            if (request.getPassword().length() < 6) {
                throw new AuthenticationException("Password too weak");
            }

            // Create new user
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setPhone(request.getPhone());
            user.setRole(User.Role.USER);
            user.setIsVerified(true); // Auto-verify for now
            user.setIsActive(true);

            User savedUser = userRepository.save(user);

            // Create user profile
            UserProfile profile = new UserProfile(savedUser);
            userProfileRepository.save(profile);

            // Generate JWT token
            String token = jwtService.generateToken(savedUser);

            return new AuthResponse(token, new UserResponse(savedUser));
        } catch (DataIntegrityViolationException e) {
            throw new AuthenticationException("Email already registered");
        } catch (Exception e) {
            throw new AuthenticationException("User creation failed");
        }
    }

    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Get user details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("User not found"));

        // Generate JWT token
        String token = jwtService.generateToken(user);

        return new AuthResponse(token, new UserResponse(user));
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        return new UserResponse(user);
    }
}

