package com.example.recipe.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.example.recipe.model.Recipe;

public interface RecipeRepository extends MongoRepository<Recipe, String> {

    // Find recipes by author ID
    List<Recipe> findByAuthorId(String authorId);

    // Find recipes by title (case-insensitive)
    List<Recipe> findByTitleIgnoreCase(String title);

    // Find recipes by cuisine type
    List<Recipe> findByCuisineIgnoreCase(String cuisine);

    // Find recipes by dietary preferences (contains)
    List<Recipe> findByDietaryPreferencesContainingIgnoreCase(String dietaryPref);

    // Full-text search on title and ingredients
    // MongoDB will use the text index we defined with @TextIndexed
    @Query("{ $text: { $search: ?0 } }")
    List<Recipe> findByTextSearch(String searchText);

    // Find recipes by ingredient name
    List<Recipe> findByIngredientNamesContainingIgnoreCase(String ingredientName);

    // Find recipe by title and author
    Optional<Recipe> findByTitleIgnoreCaseAndAuthorId(String title, String authorId);

    // Find recipes with prep time <= specified time
    List<Recipe> findByPrepTimeLessThanEqual(Long maxPrepTime);

    // Custom query to find recipes with highest favorites
    @Query("{ }")
    List<Recipe> findAllOrderByFavorites();
}