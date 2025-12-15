package com.example.recipe.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.example.recipe.model.Recipe;
import com.example.recipe.model.User;
import com.example.recipe.repository.UserRepository;
import com.example.recipe.dto.SearchRequest;
import com.example.recipe.dto.SearchResponse;
import com.example.recipe.service.RecipeService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/recipes")
@AllArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final UserRepository userRepository;

    /**
     * Get currently authenticated user ID from JWT token
     */
    private String getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Create a new recipe (authenticated users only)
     * POST /api/recipes
     */
    @PostMapping
    public ResponseEntity<?> createRecipe(@RequestBody Recipe recipe) {
        String userId = getCurrentUserId();
        Recipe createdRecipe = recipeService.createRecipe(recipe, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRecipe);
    }

    /**
     * Get all recipes
     * GET /api/recipes
     */
    @GetMapping
    public ResponseEntity<?> getAllRecipes() {
        List<Recipe> recipes = recipeService.getAllRecipes();
        return ResponseEntity.ok(recipes);
    }

    /**
     * Get a specific recipe by ID
     * GET /api/recipes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRecipeById(@PathVariable String id) {
        return recipeService.getRecipeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all recipes authored by current user
     * GET /api/recipes/user/my-recipes
     */
    @GetMapping("/user/my-recipes")
    public ResponseEntity<?> getMyRecipes() {
        String userId = getCurrentUserId();
        List<Recipe> recipes = recipeService.getRecipesByAuthor(userId);
        return ResponseEntity.ok(recipes);
    }

    /**
     * Get recipes authored by a specific user
     * GET /api/recipes/author/{userId}
     */
    @GetMapping("/author/{userId}")
    public ResponseEntity<?> getRecipesByAuthor(@PathVariable String userId) {
        List<Recipe> recipes = recipeService.getRecipesByAuthor(userId);
        return ResponseEntity.ok(recipes);
    }

    /**
     * Update a recipe (only author can update)
     * PUT /api/recipes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecipe(@PathVariable String id, @RequestBody Recipe updates) {
        String userId = getCurrentUserId();
        Recipe updatedRecipe = recipeService.updateRecipe(id, updates, userId);
        return ResponseEntity.ok(updatedRecipe);
    }

    /**
     * Delete a recipe (only author can delete)
     * DELETE /api/recipes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable String id) {
        String userId = getCurrentUserId();
        recipeService.deleteRecipe(id, userId);
        return ResponseEntity.ok("Recipe deleted successfully");
    }

    /**
     * Add recipe to user's saved/favorite list
     * POST /api/recipes/{id}/save
     */
    @PostMapping("/{id}/save")
    public ResponseEntity<?> saveRecipe(@PathVariable String id) {
        String userId = getCurrentUserId();
        recipeService.addRecipeToUserSaved(id, userId);
        return ResponseEntity.ok("Recipe saved successfully");
    }

    /**
     * Remove recipe from user's saved list
     * DELETE /api/recipes/{id}/unsave
     */
    @DeleteMapping("/{id}/unsave")
    public ResponseEntity<?> unsaveRecipe(@PathVariable String id) {
        String userId = getCurrentUserId();
        recipeService.removeRecipeFromUserSaved(id, userId);
        return ResponseEntity.ok("Recipe removed from saved");
    }

    /**
     * Get all recipes saved by current user
     * GET /api/recipes/user/saved
     */
    @GetMapping("/user/saved")
    public ResponseEntity<?> getMySavedRecipes() {
        String userId = getCurrentUserId();
        List<Recipe> recipes = recipeService.getUserSavedRecipes(userId);
        return ResponseEntity.ok(recipes);
    }

    /**
     * Search recipes by cuisine
     * GET /api/recipes/search/cuisine?type=Italian
     */
    @GetMapping("/search/cuisine")
    public ResponseEntity<?> searchByCuisine(@RequestParam String type) {
        List<Recipe> recipes = recipeService.getRecipesByCuisine(type);
        return ResponseEntity.ok(recipes);
    }

    /**
     * Search recipes by title
     * GET /api/recipes/search/title?name=pasta
     */
    @GetMapping("/search/title")
    public ResponseEntity<?> searchByTitle(@RequestParam String name) {
        List<Recipe> recipes = recipeService.searchRecipesByTitle(name);
        return ResponseEntity.ok(recipes);
    }

    /**
     * Search recipes by dietary tag
     * GET /api/recipes/search/dietary?tag=vegan
     */
    @GetMapping("/search/dietary")
    public ResponseEntity<?> searchByDietaryTag(@RequestParam String tag) {
        List<Recipe> recipes = recipeService.getRecipesByDietaryTag(tag);
        return ResponseEntity.ok(recipes);
    }

    /**
     * UNIFIED SEARCH ENDPOINT - Combines all search and filtering in one place
     * 
     * GET
     * /api/recipes/search?ingredient=chicken&diet=Keto&cuisine=Egyptian&maxPrepTime=30&sortBy=preptime&page=0&limit=10
     * 
     * All parameters are optional. Examples:
     * - Get all recipes: GET /api/recipes/search
     * - Search by ingredient: GET /api/recipes/search?ingredient=chicken
     * - Filter by diet: GET /api/recipes/search?diet=Vegan
     * - Multiple filters: GET
     * /api/recipes/search?ingredient=chicken&diet=Keto&cuisine=Egyptian
     * - Prep time filter: GET /api/recipes/search?maxPrepTime=30
     * - Sort options: sortBy=preptime, favorites, relevance, createdAt
     * - Pagination: page=0&limit=10 (default: page 0, 10 items)
     * 
     * @param searchRequest DTO containing all optional filters
     * @return SearchResponse with paginated results and metadata
     */
    @GetMapping("/search")
    public ResponseEntity<?> search(@Valid SearchRequest searchRequest) {
        SearchResponse results = recipeService.advancedSearch(searchRequest);
        return ResponseEntity.ok(results);
    }
}
