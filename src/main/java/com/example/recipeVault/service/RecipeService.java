package com.example.recipeVault.service;

import com.example.recipeVault.model.Recipe;
import com.example.recipeVault.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final GeminiService geminiService;

    @Autowired
    public RecipeService(RecipeRepository recipeRepository, GeminiService geminiService) {
        this.recipeRepository = recipeRepository;
        this.geminiService = geminiService;
    }

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public Optional<Recipe> getRecipeById(Long id) {
        return recipeRepository.findById(id);
    }

    public Recipe saveRecipe(Recipe recipe) {
        return recipeRepository.save(recipe);
    }

    public void deleteRecipe(Long id) {
        recipeRepository.deleteById(id);
    }

    public String getHealthTips(Recipe recipe) {
        String recipeDetails = "Title: " + recipe.getTitle() + 
                               "\nIngredients: " + recipe.getIngredients();
        return geminiService.getHealthTips(recipeDetails);
    }

    public String getIngredientAlternatives(String ingredient) {
        return geminiService.getIngredientAlternatives(ingredient);
    }

    public String getAllergySafeSubstitutes(String ingredient, String allergies) {
        return geminiService.getAllergySafeSubstitutes(ingredient, allergies);
    }
}
