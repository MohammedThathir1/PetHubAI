package com.PetHubAI.PetHubAIBackend.exception;

public class AdoptionRequestException extends RuntimeException {
    public AdoptionRequestException(String message) {
        super(message);
    }

    public AdoptionRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
