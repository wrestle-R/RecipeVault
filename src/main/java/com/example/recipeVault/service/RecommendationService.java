package com.example.recipeVault.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService {

    @Autowired
    private GeminiService geminiService;

    public String getRecommendation(String mealType, String dietaryPreferences, 
                                  String hungerLevel, String prepTime,
                                  String ingredients, String fitnessGoal, 
                                  String cuisine, String equipment) {
        try {
            String prompt = String.format(
                "As a professional nutritionist and chef, recommend a meal based on these preferences:\n" +
                "- Meal Type: %s\n" +
                "- Dietary Preferences: %s\n" +
                "- Hunger Level: %s\n" +
                "- Preparation Time: %s\n" +
                "- Ingredients (to include/avoid): %s\n" +
                "- Fitness Goal: %s\n" +
                "- Preferred Cuisine: %s\n" +
                "- Available Equipment: %s\n\n" +
                "Provide a detailed recommendation including:\n" +
                "1. Specific meal suggestion\n" +
                "2. Why it matches their needs\n" +
                "3. Quick preparation instructions\n" +
                "4. Nutritional benefits\n" +
                "Format the response in markdown. dont make it very big keep it consise and keep it simple.",
                mealType, dietaryPreferences, hungerLevel, prepTime,
                ingredients, fitnessGoal, cuisine, equipment
            );

            return geminiService.getAIResponse(prompt);
        } catch (Exception e) {
            return "Error generating recommendation: " + e.getMessage();
        }
    }

    public String getFollowUpResponse(String question) {
        try {
            String prompt = String.format(
                "As a follow-up to the previous recipe recommendation, please answer this question: %s\n" +
                "Provide a clear, detailed response formatted in markdown.",
                question
            );
            return geminiService.getAIResponse(prompt);
        } catch (Exception e) {
            return "Error generating response: " + e.getMessage();
        }
    }

    public String getFollowUpResponse(String question, String initialRecommendation, List<Map<String, String>> history) {
        try {
            StringBuilder contextBuilder = new StringBuilder();
            
            // Add the initial recommendation as context
            contextBuilder.append("Initial recommendation:\n");
            contextBuilder.append(initialRecommendation);
            contextBuilder.append("\n\n");
            
            // Add conversation history
            if (history != null && !history.isEmpty()) {
                contextBuilder.append("Conversation history:\n");
                for (Map<String, String> message : history) {
                    String role = message.get("role");
                    String content = message.get("content");
                    // Don't include the current question again
                    if (role.equals("user") && content.equals(question)) {
                        continue;
                    }
                    contextBuilder.append(role).append(": ").append(content).append("\n\n");
                }
            }
            
            String prompt = String.format(
                "As a culinary AI assistant, answer this follow-up question: \"%s\"\n\n" +
                "Use the following context to inform your response:\n%s\n" +
                "Provide a clear, concise response formatted in markdown. If the question " +
                "is about ingredients, steps, or nutrition, format your answer appropriately " +
                "for that specific aspect of the recipe.",
                question, contextBuilder.toString()
            );
            
            return geminiService.getAIResponse(prompt);
        } catch (Exception e) {
            return "Error generating response: " + e.getMessage();
        }
    }
}
