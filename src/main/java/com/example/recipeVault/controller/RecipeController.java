package com.example.recipeVault.controller;

import com.example.recipeVault.model.Recipe;
import com.example.recipeVault.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class RecipeController {

    private final RecipeService recipeService;

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
}
