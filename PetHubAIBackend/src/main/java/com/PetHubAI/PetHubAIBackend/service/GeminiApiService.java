package com.PetHubAI.PetHubAIBackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GeminiApiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    // Rate limiting tracking
    private final Map<String, List<LocalDateTime>> requestHistory = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 8; // Conservative limit

    public String generateResponse(String prompt) {
        try {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return "Please configure your Gemini API key in your environment variables.";
            }

            // Check rate limit before making request
            if (isRateLimited()) {
                return "I'm getting a lot of requests right now! Please wait a moment before asking again. ⏰";
            }

            // Record this request
            recordRequest();

            Map<String, Object> requestBody = createRequestBody(prompt);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = GEMINI_API_URL + "?key=" + apiKey;
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            return parseResponse(response.getBody());

        } catch (HttpClientErrorException e) {
            // Handle specific HTTP errors
            if (e.getStatusCode().value() == 429) {
                return "I'm receiving too many requests! Please wait about a minute before trying again. ⏱️";
            } else if (e.getStatusCode().value() == 403) {
                return "There seems to be an issue with my API access. Please try again later! 🔑";
            } else {
                System.err.println("Gemini API HTTP Error: " + e.getStatusCode() + " - " + e.getMessage());
                return "I encountered an API error. Let's try again in a few moments! 🤖";
            }
        } catch (Exception e) {
            System.err.println("Gemini API Error: " + e.getMessage());
            return "I'm having a technical hiccup! Please try asking your question again. 🛠️";
        }
    }

    private boolean isRateLimited() {
        List<LocalDateTime> requests = requestHistory.computeIfAbsent("global", k -> new ArrayList<>());

        // Clean up old requests (older than 1 minute)
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minus(1, ChronoUnit.MINUTES);
        requests.removeIf(requestTime -> requestTime.isBefore(oneMinuteAgo));

        // Check if we're at the limit
        return requests.size() >= MAX_REQUESTS_PER_MINUTE;
    }

    private void recordRequest() {
        List<LocalDateTime> requests = requestHistory.computeIfAbsent("global", k -> new ArrayList<>());
        requests.add(LocalDateTime.now());
    }

    private Map<String, Object> createRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();
        Map<String, Object> part = new HashMap<>();

        part.put("text", prompt);
        parts.add(part);
        content.put("parts", parts);
        contents.add(content);
        requestBody.put("contents", contents);

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 800); // Reduced to save quota
        requestBody.put("generationConfig", generationConfig);

        return requestBody;
    }

    @SuppressWarnings("unchecked")
    private String parseResponse(Map<String, Object> responseBody) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                if (content != null) {
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        String text = (String) parts.get(0).get("text");
                        return text != null ? text.trim() : "I couldn't generate a proper response. Please try again!";
                    }
                }
            }
            return "I'm having trouble forming a response right now. Please rephrase your question! 🤔";
        } catch (Exception e) {
            System.err.println("Response parsing error: " + e.getMessage());
            return "I had trouble understanding my own response! Please ask again. 😅";
        }
    }
}
