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
import com.example.recipe.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse<Recipe>> createRecipe(@RequestBody Recipe recipe) {
        String userId = getCurrentUserId();
        Recipe createdRecipe = recipeService.createRecipe(recipe, userId);
        ApiResponse<Recipe> response = ApiResponse.success("Recipe created", createdRecipe, HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all recipes
     * GET /api/recipes
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Recipe>>> getAllRecipes() {
        List<Recipe> recipes = recipeService.getAllRecipes();
        ApiResponse<List<Recipe>> response = ApiResponse.success("Recipes fetched", recipes, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific recipe by ID
     * GET /api/recipes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Recipe>> getRecipeById(@PathVariable String id) {
        return recipeService.getRecipeById(id)
                .map(recipe -> ResponseEntity.ok(ApiResponse.success("Recipe fetched", recipe, HttpStatus.OK.value())))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure("Recipe not found", HttpStatus.NOT_FOUND.value())));
    }

    /**
     * Get all recipes authored by current user
     * GET /api/recipes/user/my-recipes
     */
    @GetMapping("/user/my-recipes")
    public ResponseEntity<ApiResponse<List<Recipe>>> getMyRecipes() {
        String userId = getCurrentUserId();
        List<Recipe> recipes = recipeService.getRecipesByAuthor(userId);
        ApiResponse<List<Recipe>> response = ApiResponse.success("My recipes fetched", recipes, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Get recipes authored by a specific user
     * GET /api/recipes/author/{userId}
     */
    @GetMapping("/author/{userId}")
    public ResponseEntity<ApiResponse<List<Recipe>>> getRecipesByAuthor(@PathVariable String userId) {
        List<Recipe> recipes = recipeService.getRecipesByAuthor(userId);
        ApiResponse<List<Recipe>> response = ApiResponse.success("Author recipes fetched", recipes,
                HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Update a recipe (only author can update)
     * PUT /api/recipes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Recipe>> updateRecipe(@PathVariable String id, @RequestBody Recipe updates) {
        String userId = getCurrentUserId();
        Recipe updatedRecipe = recipeService.updateRecipe(id, updates, userId);
        ApiResponse<Recipe> response = ApiResponse.success("Recipe updated", updatedRecipe, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a recipe (only author can delete)
     * DELETE /api/recipes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecipe(@PathVariable String id) {
        String userId = getCurrentUserId();
        recipeService.deleteRecipe(id, userId);
        ApiResponse<Void> response = ApiResponse.success("Recipe deleted successfully", null, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Add recipe to user's saved/favorite list
     * POST /api/recipes/{id}/save
     */
    @PostMapping("/{id}/save")
    public ResponseEntity<ApiResponse<Void>> saveRecipe(@PathVariable String id) {
        String userId = getCurrentUserId();
        recipeService.addRecipeToUserSaved(id, userId);
        ApiResponse<Void> response = ApiResponse.success("Recipe saved successfully", null, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Remove recipe from user's saved list
     * DELETE /api/recipes/{id}/unsave
     */
    @DeleteMapping("/{id}/unsave")
    public ResponseEntity<ApiResponse<Void>> unsaveRecipe(@PathVariable String id) {
        String userId = getCurrentUserId();
        recipeService.removeRecipeFromUserSaved(id, userId);
        ApiResponse<Void> response = ApiResponse.success("Recipe removed from saved", null, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all recipes saved by current user
     * GET /api/recipes/user/saved
     */
    @GetMapping("/user/saved")
    public ResponseEntity<ApiResponse<List<Recipe>>> getMySavedRecipes() {
        String userId = getCurrentUserId();
        List<Recipe> recipes = recipeService.getUserSavedRecipes(userId);
        ApiResponse<List<Recipe>> response = ApiResponse.success("Saved recipes fetched", recipes,
                HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Search recipes by cuisine
     * GET /api/recipes/search/cuisine?type=Italian
     */
    @GetMapping("/search/cuisine")
    public ResponseEntity<ApiResponse<List<Recipe>>> searchByCuisine(@RequestParam String type) {
        List<Recipe> recipes = recipeService.getRecipesByCuisine(type);
        ApiResponse<List<Recipe>> response = ApiResponse.success("Recipes by cuisine fetched", recipes,
                HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Search recipes by title
     * GET /api/recipes/search/title?name=pasta
     */
    @GetMapping("/search/title")
    public ResponseEntity<ApiResponse<List<Recipe>>> searchByTitle(@RequestParam String name) {
        List<Recipe> recipes = recipeService.searchRecipesByTitle(name);
        ApiResponse<List<Recipe>> response = ApiResponse.success("Recipes by title fetched", recipes,
                HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Search recipes by dietary tag
     * GET /api/recipes/search/dietary?tag=vegan
     */
    @GetMapping("/search/dietary")
    public ResponseEntity<ApiResponse<List<Recipe>>> searchByDietaryTag(@RequestParam String tag) {
        List<Recipe> recipes = recipeService.getRecipesByDietaryTag(tag);
        ApiResponse<List<Recipe>> response = ApiResponse.success("Recipes by dietary tag fetched", recipes,
                HttpStatus.OK.value());
        return ResponseEntity.ok(response);
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
    public ResponseEntity<ApiResponse<SearchResponse>> search(@Valid SearchRequest searchRequest) {
        SearchResponse results = recipeService.advancedSearch(searchRequest);
        ApiResponse<SearchResponse> response = ApiResponse.success("Search results", results, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
}
