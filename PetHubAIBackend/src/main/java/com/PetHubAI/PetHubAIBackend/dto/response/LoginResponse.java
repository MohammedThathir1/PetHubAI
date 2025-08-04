package com.PetHubAI.PetHubAIBackend.dto.response;


import com.PetHubAI.PetHubAIBackend.entity.User;

public class LoginResponse {
    private String token;
    private UserResponse user;

    public LoginResponse(String token, User user) {
        this.token = token;
        this.user = new UserResponse(user);
    }

    // Getters and setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }
}
