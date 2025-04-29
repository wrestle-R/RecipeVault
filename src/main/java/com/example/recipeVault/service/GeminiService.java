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

import io.github.cdimascio.dotenv.Dotenv;


@Service
public class GeminiService {

    // Remove static Dotenv instance
    private final String apiKey; // Keep apiKey final
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // Constructor to load Dotenv and API key
    public GeminiService() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load(); // Load .env, ignore if missing
        this.apiKey = dotenv.get("GEMINI_API_KEY"); 

        // Optional: Add a check or warning if the key is not found
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            System.err.println("WARNING: GEMINI_API_KEY not found in .env file or environment variables. API calls may fail.");
            // Depending on requirements, you might want to throw an exception here
            // throw new IllegalStateException("GEMINI_API_KEY is not configured.");
        }
    }
    
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
    
    public String getAIResponse(String prompt) {
        try {
            return callGeminiApi(prompt);
        } catch (Exception e) {
            return "Error generating response: " + e.getMessage();
        }
    }
    
    private String callGeminiApi(String prompt) throws Exception {
        // Check if API key is available before making the call
        if (apiKey == null || apiKey.isEmpty()) {
             return "Error: Gemini API Key is missing or not configured.";
        }
        
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
        // System.out.println("Request body: " + requestBody); // Consider removing in production
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Response status: " + response.statusCode());
        // System.out.println("Response body: " + response.body()); // Consider removing or shortening in production
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            // Parse the response to extract just the generated text
            Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
            try {
                return extractTextFromGeminiResponse(responseMap);
            } catch (Exception e) {
                System.err.println("Error processing response: " + e.getMessage());
                System.err.println("Response body was: " + response.body()); // Log failing body
                return "We're having trouble processing the response from the AI service. Please try again later.";
            }
        } else {
            // Log the error response body for debugging
            System.err.println("Error response body from Gemini API: " + response.body());
            return "Error from Gemini API: HTTP " + response.statusCode() + 
                   ". Please check your API key configuration or try again later."; 
        }
    }
    
    private String extractTextFromGeminiResponse(Map<String, Object> responseMap) {
        try {
            // First try the standard response format
            if (responseMap.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    // Check for finishReason STOP before accessing content
                    if (candidate.containsKey("finishReason") && "STOP".equals(candidate.get("finishReason"))) {
                        Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                        if (content != null && content.containsKey("parts")) {
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                            if (parts != null && !parts.isEmpty() && parts.get(0).containsKey("text")) {
                                return (String) parts.get(0).get("text");
                            }
                        }
                    } else {
                         // Handle other finish reasons (e.g., SAFETY, MAX_TOKENS)
                         String reason = candidate.containsKey("finishReason") ? (String) candidate.get("finishReason") : "UNKNOWN";
                         System.err.println("Gemini API response finished with reason: " + reason);
                         // Check for safety ratings if available
                         if (candidate.containsKey("safetyRatings")) {
                             System.err.println("Safety Ratings: " + candidate.get("safetyRatings"));
                         }
                         return "AI response blocked or incomplete. Reason: " + reason;
                    }
                }
            }
            
            // Fallback checks (less common for current API versions)
            if (responseMap.containsKey("text")) {
                return (String) responseMap.get("text");
            }
            if (responseMap.containsKey("content") && responseMap.get("content") instanceof Map) {
                Map<String, Object> content = (Map<String, Object>) responseMap.get("content");
                if (content.containsKey("text")) {
                    return (String) content.get("text");
                }
            }
            
            // If no valid text found
             System.err.println("Could not extract text from Gemini response structure: " + responseMap);
            return "Response received but no valid text content found.";
        } catch (ClassCastException e) {
             System.err.println("Error parsing response structure (ClassCastException): " + e.getMessage());
             System.err.println("Response map structure: " + responseMap);
             return "Error parsing the structure of the response from Gemini API.";
        } catch (Exception e) {
            System.err.println("Generic error parsing response: " + e.getMessage());
            System.err.println("Response map structure: " + responseMap);
            return "Error parsing response from Gemini API. Please try again.";
        }
    }
}
