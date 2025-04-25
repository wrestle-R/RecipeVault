package com.example.recipeVault.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // Import ResponseEntity
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable; // Import GeminiService
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.recipeVault.model.Recipe;
import com.example.recipeVault.service.GeminiService;
import com.example.recipeVault.service.RecipeService;

@Controller
public class RecipeController {

    private final RecipeService recipeService;
    
    @Autowired // Inject GeminiService
    private GeminiService geminiService;
    
    @Autowired
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("recipes", recipeService.getAllRecipes());
        return "index";
    }

    @GetMapping("/add-recipe")
    public String showAddRecipeForm(Model model) {
        model.addAttribute("recipe", new Recipe());
        return "add-recipe";
    }

    @PostMapping("/add-recipe")
    public String addRecipe(@ModelAttribute Recipe recipe, @RequestParam("tagsInput") String tagsInput) {
        // Process tags input
        if (tagsInput != null && !tagsInput.trim().isEmpty()) {
            List<String> tags = Arrays.stream(tagsInput.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList());
            recipe.setTags(tags);
        }
        
        recipeService.saveRecipe(recipe);
        return "redirect:/";
    }

    @GetMapping("/view-recipe/{id}")
    public String viewRecipe(@PathVariable Long id, Model model) {
        Optional<Recipe> recipe = recipeService.getRecipeById(id);
        if (recipe.isPresent()) {
            model.addAttribute("recipe", recipe.get());
            model.addAttribute("healthTips", recipeService.getHealthTips(recipe.get()));
            return "view-recipe";
        }
        return "redirect:/";
    }

    @GetMapping("/delete-recipe/{id}")
    public String deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return "redirect:/";
    }
    
    @GetMapping("/api/alternatives")
    @ResponseBody
    public String getAlternatives(@RequestParam String ingredient) {
        return recipeService.getIngredientAlternatives(ingredient);
    }
    
    @GetMapping("/api/allergy-substitutes")
    @ResponseBody
    public String getAllergySubstitutes(@RequestParam String ingredient, @RequestParam String allergies) {
        return recipeService.getAllergySafeSubstitutes(ingredient, allergies);
    }

    // --- NEW ENDPOINT for Health Tips ---
    @GetMapping("/api/recipes/{id}/health-tips")
    @ResponseBody
    public ResponseEntity<String> getHealthTips(@PathVariable Long id) {
        Optional<Recipe> recipeOpt = recipeService.getRecipeById(id);
        if (recipeOpt.isPresent()) {
            Recipe recipe = recipeOpt.get();
            // Construct details string for the prompt
            String recipeDetails = "Recipe Title: " + recipe.getTitle() + "\n" +
                                   "Ingredients: " + recipe.getIngredients() + "\n" +
                                   "Instructions: " + recipe.getInstructions(); // Consider if instructions are needed/too long
            
            String tips = geminiService.getHealthTips(recipeDetails);
            return ResponseEntity.ok(tips);
        } else {
            return ResponseEntity.notFound().build(); // Return 404 if recipe not found
        }
    }
}
