package com.example.recipe.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.example.recipe.model.Recipe;

public interface RecipeRepository extends MongoRepository<Recipe, String> {

    List<Recipe> findByAuthorId(String authorId);
   
    List<Recipe> findByTitleIgnoreCase(String title);
  
    List<Recipe> findByCuisineIgnoreCase(String cuisine);

    List<Recipe> findByDietaryPreferencesContainingIgnoreCase(String dietaryPref);

    @Query("{ $text: { $search: ?0 } }")
    List<Recipe> findByTextSearch(String searchText);

    List<Recipe> findByIngredientNamesContainingIgnoreCase(String ingredientName);

    Optional<Recipe> findByTitleIgnoreCaseAndAuthorId(String title, String authorId);

    List<Recipe> findByPrepTimeLessThanEqual(Long maxPrepTime);

    @Query("{ }")
    List<Recipe> findAllOrderByFavorites();
}