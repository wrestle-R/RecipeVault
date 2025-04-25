package com.example.recipeVault.service;

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
}
