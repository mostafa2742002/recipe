package com.example.recipe.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.recipe.model.Recipe;
import com.example.recipe.model.User;
import com.example.recipe.repository.RecipeRepository;
import com.example.recipe.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    /**
     * Create a new recipe and automatically link it to the author
     */
    public Recipe createRecipe(Recipe recipe, String userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Set the author automatically
        recipe.setAuthor(author);

        // Save the recipe
        Recipe savedRecipe = recipeRepository.save(recipe);

        // Add recipe to user's authored recipes
        author.getRecipesAuthored().add(savedRecipe);
        userRepository.save(author);

        return savedRecipe;
    }

    /**
     * Get all recipes authored by a specific user
     */
    public List<Recipe> getRecipesByAuthor(String userId) {
        return recipeRepository.findByAuthorId(userId);
    }

    /**
     * Get a single recipe by ID
     */
    public Optional<Recipe> getRecipeById(String recipeId) {
        return recipeRepository.findById(recipeId);
    }

    /**
     * Update an existing recipe (only author can update)
     */
    public Recipe updateRecipe(String recipeId, Recipe updates, String userId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found with id: " + recipeId));

        // Verify user is the author
        if (!recipe.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Only the author can update this recipe");
        }

        // Update fields
        if (updates.getTitle() != null)
            recipe.setTitle(updates.getTitle());
        if (updates.getImage() != null)
            recipe.setImage(updates.getImage());
        if (updates.getCuisine() != null)
            recipe.setCuisine(updates.getCuisine());
        if (updates.getPrepTime() != null)
            recipe.setPrepTime(updates.getPrepTime());
        if (updates.getIngredients() != null)
            recipe.setIngredients(updates.getIngredients());
        if (updates.getSteps() != null)
            recipe.setSteps(updates.getSteps());
        if (updates.getDietaryTags() != null)
            recipe.setDietaryTags(updates.getDietaryTags());

        // UpdatedAt is handled automatically by @LastModifiedDate
        return recipeRepository.save(recipe);
    }

    /**
     * Delete a recipe (only author can delete)
     */
    public void deleteRecipe(String recipeId, String userId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found with id: " + recipeId));

        // Verify user is the author
        if (!recipe.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Only the author can delete this recipe");
        }

        // Remove recipe from user's authored recipes
        User author = recipe.getAuthor();
        author.getRecipesAuthored().remove(recipe);
        userRepository.save(author);

        // Delete the recipe
        recipeRepository.deleteById(recipeId);
    }

    /**
     * Add a recipe to user's saved recipes (favorite)
     */
    public void addRecipeToUserSaved(String recipeId, String userId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found with id: " + recipeId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Add recipe to user's saved recipes
        if (!user.getRecipesSaved().contains(recipe)) {
            user.getRecipesSaved().add(recipe);
            recipe.setFavoritesCount((recipe.getFavoritesCount() != null ? recipe.getFavoritesCount() : 0) + 1);

            userRepository.save(user);
            recipeRepository.save(recipe);
        }
    }

    /**
     * Remove a recipe from user's saved recipes
     */
    public void removeRecipeFromUserSaved(String recipeId, String userId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found with id: " + recipeId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Remove recipe from user's saved recipes
        if (user.getRecipesSaved().remove(recipe)) {
            recipe.setFavoritesCount((recipe.getFavoritesCount() != null ? recipe.getFavoritesCount() : 0) - 1);

            userRepository.save(user);
            recipeRepository.save(recipe);
        }
    }

    /**
     * Get all recipes saved by a user
     */
    public List<Recipe> getUserSavedRecipes(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return user.getRecipesSaved().stream().toList();
    }

    /**
     * Get recipes by cuisine type
     */
    public List<Recipe> getRecipesByCuisine(String cuisine) {
        return recipeRepository.findByCuisineIgnoreCase(cuisine);
    }

    /**
     * Search recipes by title
     */
    public List<Recipe> searchRecipesByTitle(String title) {
        return recipeRepository.findByTitleIgnoreCase(title);
    }

    /**
     * Get recipes by dietary tags
     */
    public List<Recipe> getRecipesByDietaryTag(String dietaryTag) {
        return recipeRepository.findByDietaryTagsContainingIgnoreCase(dietaryTag);
    }

    /**
     * Get all recipes (with potential pagination in real app)
     */
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }
}
