package com.example.recipeVault.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.recipeVault.service.RecommendationService;

@Controller
public class RecipeRecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/chatbot")
    public String showRecommendationForm() {
        return "chatbot";
    }

    @PostMapping("/api/recommend")
    @ResponseBody
    public String getRecommendation(@RequestParam String mealType,
                                  @RequestParam String dietaryPreferences,
                                  @RequestParam String hungerLevel,
                                  @RequestParam String prepTime,
                                  @RequestParam String ingredients,
                                  @RequestParam String fitnessGoal,
                                  @RequestParam String cuisine,
                                  @RequestParam String equipment) {
        return recommendationService.getRecommendation(
            mealType, dietaryPreferences, hungerLevel, prepTime,
            ingredients, fitnessGoal, cuisine, equipment
        );
    }

    @PostMapping("/api/followup")
    @ResponseBody
    public String handleFollowUp(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        return recommendationService.getFollowUpResponse(question);
    }
}
