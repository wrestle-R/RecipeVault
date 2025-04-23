package com.example.recipeVault.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeminiService {

    // Using the API key directly instead of loading from properties
    private final String apiKey = "your-key-here"; // Replace with your actual
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    public String getIngredientAlternatives(String ingredient) {
        try {
            String prompt = "Suggest 5 alternative ingredients for: " + ingredient + ". Please keep your answer concise and focus only on listing alternatives.";
            return callGeminiApi(prompt);
        } catch (Exception e) {
            return "Could not generate alternatives: " + e.getMessage();
        }
    }
    
    public String getAllergySafeSubstitutes(String ingredient, String allergies) {
        try {
            String prompt = "Suggest 3-5 allergy-safe substitutes for " + ingredient + 
                            " for someone with these allergies: " + allergies + ". Please keep your answer concise.";
            return callGeminiApi(prompt);
        } catch (Exception e) {
            return "Could not generate substitutes: " + e.getMessage();
        }
    }
    
    public String getHealthTips(String recipeDetails) {
        try {
            String prompt = "Based on these recipe details, provide 2-3 brief health tips or warnings (max 2 sentences each): " + recipeDetails + 
                           ". Focus on nutritional value, allergies, or health considerations.";
            return callGeminiApi(prompt);
        } catch (Exception e) {
            return "Could not generate health tips: " + e.getMessage();
        }
    }
    
    private String callGeminiApi(String prompt) throws Exception {
        // Updated URL to use the newer model and correct endpoint
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> contents = new HashMap<>();
        contents.put("role", "user");
        contents.put("parts", new Object[]{Map.of("text", prompt)});
        data.put("contents", new Object[]{contents});
        
        // Add generation config to improve reliability
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.4);
        generationConfig.put("topK", 32);
        generationConfig.put("topP", 1.0);
        generationConfig.put("maxOutputTokens", 1024);
        data.put("generationConfig", generationConfig);
        
        String requestBody = objectMapper.writeValueAsString(data);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();
        
        System.out.println("Calling Gemini API with URL: " + url);
        System.out.println("Request body: " + requestBody);
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Response status: " + response.statusCode());
        System.out.println("Response body: " + response.body());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            // Parse the response to extract just the generated text
            Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
            try {
                return extractTextFromGeminiResponse(responseMap);
            } catch (Exception e) {
                System.err.println("Error processing response: " + e.getMessage());
                return "We're having trouble processing this request at the moment. Please try again later.";
            }
        } else {
            return "Error from Gemini API: HTTP " + response.statusCode() + 
                   ". Pleases check your API key or try again later.";
        }
    }
    
    private String extractTextFromGeminiResponse(Map<String, Object> responseMap) {
        try {
            // First try the standard response format
            if (responseMap.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
            
            // If we get here, try alternative formats
            if (responseMap.containsKey("text")) {
                return (String) responseMap.get("text");
            }
            
            if (responseMap.containsKey("content")) {
                Map<String, Object> content = (Map<String, Object>) responseMap.get("content");
                if (content.containsKey("text")) {
                    return (String) content.get("text");
                }
            }
            
            // If all else fails
            return "Response received but no text content found.";
        } catch (Exception e) {
            return "Error parsing response from Gemini API. Please try again.";
        }
    }
}
