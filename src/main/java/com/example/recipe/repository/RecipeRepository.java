package com.example.recipe.repository;

import java.util.List;
import java.util.Optional;

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

    // Find recipes by dietary tags
    List<Recipe> findByDietaryTagsContainingIgnoreCase(String dietaryTag);

    // Custom query to find recipes with highest favorites
    @Query("{ }")
    List<Recipe> findAllOrderByFavorites();

    // Find recipe by title and author
    Optional<Recipe> findByTitleIgnoreCaseAndAuthorId(String title, String authorId);
}
