package com.example.recipe.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.example.recipe.dto.RecipeSearchResult;
import com.example.recipe.dto.SearchRequest;
import com.example.recipe.dto.SearchResponse;
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
    private final MongoTemplate mongoTemplate;

    /**
     * Create a new recipe and automatically link it to the author
     */
    public Recipe createRecipe(Recipe recipe, String userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        recipe.setAuthor(author);
        Recipe savedRecipe = recipeRepository.save(recipe);

        author.getRecipesAuthored().add(savedRecipe);
        userRepository.save(author);

        return savedRecipe;
    }

    /**
     * Advanced search with MongoDB Aggregation Pipeline
     * 
     * MongoDB Aggregation is a powerful framework for data transformation.
     * Think of it like an assembly line where data passes through multiple stages.
     * 
     * STAGES:
     * 1. $match: Filter documents (like WHERE in SQL)
     * 2. $lookup: Join with another collection (like SQL JOIN)
     * 3. $sort: Order results
     * 4. $skip/$limit: Pagination
     * 5. $project: Select which fields to return
     */
    public SearchResponse advancedSearch(SearchRequest searchRequest) {
        List<Criteria> filters = new ArrayList<>();

        // TEXT SEARCH: Searches all @TextIndexed fields (title, ingredients)
        if (searchRequest.getSearchText() != null && !searchRequest.getSearchText().isBlank()) {
            filters.add(Criteria.where("$text").is(searchRequest.getSearchText()));
        }

        // INGREDIENT FILTER: Find recipes containing specific ingredient
        if (searchRequest.getIngredient() != null && !searchRequest.getIngredient().isBlank()) {
            filters.add(Criteria.where("ingredientNames")
                    .regex(searchRequest.getIngredient(), "i"));
        }

        // CUISINE FILTER
        if (searchRequest.getCuisine() != null && !searchRequest.getCuisine().isBlank()) {
            filters.add(Criteria.where("cuisine")
                    .regex(searchRequest.getCuisine(), "i"));
        }

        // DIETARY PREFERENCE FILTER
        if (searchRequest.getDietaryPreference() != null && !searchRequest.getDietaryPreference().isBlank()) {
            filters.add(Criteria.where("dietaryPreferences")
                    .in(searchRequest.getDietaryPreference()));
        }

        // PREP TIME FILTER: Show recipes with prepTime <= maxPrepTime
        if (searchRequest.getMaxPrepTime() != null && searchRequest.getMaxPrepTime() > 0) {
            filters.add(Criteria.where("prepTime")
                    .lte(searchRequest.getMaxPrepTime()));
        }

        // Combine all filters with AND logic
        Criteria criteria = new Criteria();
        if (!filters.isEmpty()) {
            criteria = new Criteria().andOperator(filters.toArray(new Criteria[0]));
        }

        // Build aggregation pipeline stages
        List<AggregationOperation> operations = new ArrayList<>();

        // STAGE 1: $match - Filter documents
        operations.add(Aggregation.match(criteria));

        // STAGE 2: $lookup - Join with User collection for author info
        operations.add(Aggregation.lookup("users", "author._id", "_id", "authorInfo"));

        // STAGE 3: $sort - Order results
        String sortField = "createdAt";
        if (searchRequest.getSortBy() != null) {
            sortField = switch (searchRequest.getSortBy().toLowerCase()) {
                case "preptime" -> "prepTime";
                case "favorites" -> "favoritesCount";
                default -> "createdAt";
            };
        }
        operations.add(Aggregation.sort(Sort.Direction.DESC, sortField));

        // STAGE 4: $skip - Skip documents for pagination
        int skip = searchRequest.getPage() * searchRequest.getLimit();
        operations.add(Aggregation.skip((long) skip));

        // STAGE 5: $limit - Limit results per page
        operations.add(Aggregation.limit(searchRequest.getLimit().longValue()));

        // STAGE 6: $project - Select fields to return
        operations.add(Aggregation.project()
                .and("_id").as("id")
                .and("title").as("title")
                .and("image").as("image")
                .and("cuisine").as("cuisine")
                .and("prepTime").as("prepTime")
                .and("ingredientNames").as("ingredientNames")
                .and("dietaryPreferences").as("dietaryPreferences")
                .and("favoritesCount").as("favoritesCount")
                .and("authorInfo.username").as("authorName"));

        // Execute aggregation pipeline
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<RecipeSearchResult> results = mongoTemplate.aggregate(
                aggregation,
                "recipes",
                RecipeSearchResult.class);

        List<RecipeSearchResult> recipes = results.getMappedResults();

        // Calculate total count and pages
        long totalCount = mongoTemplate.count(Query.query(criteria), Recipe.class);
        int totalPages = (int) Math.ceil((double) totalCount / searchRequest.getLimit());

        return new SearchResponse(recipes, totalCount, searchRequest.getPage(), totalPages);
    }

    // ===== ORIGINAL METHODS =====

    public List<Recipe> getRecipesByAuthor(String userId) {
        return recipeRepository.findByAuthorId(userId);
    }

    public Optional<Recipe> getRecipeById(String recipeId) {
        return recipeRepository.findById(recipeId);
    }

    public Recipe updateRecipe(String recipeId, Recipe updates, String userId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found with id: " + recipeId));

        if (!recipe.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Only the author can update this recipe");
        }

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
        if (updates.getIngredientNames() != null)
            recipe.setIngredientNames(updates.getIngredientNames());
        if (updates.getSteps() != null)
            recipe.setSteps(updates.getSteps());
        if (updates.getDietaryPreferences() != null)
            recipe.setDietaryPreferences(updates.getDietaryPreferences());

        return recipeRepository.save(recipe);
    }

    public void deleteRecipe(String recipeId, String userId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found with id: " + recipeId));

        if (!recipe.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Only the author can delete this recipe");
        }

        User author = recipe.getAuthor();
        author.getRecipesAuthored().remove(recipe);
        userRepository.save(author);

        recipeRepository.deleteById(recipeId);
    }

    public void addRecipeToUserSaved(String recipeId, String userId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found with id: " + recipeId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (!user.getRecipesSaved().contains(recipe)) {
            user.getRecipesSaved().add(recipe);
            recipe.setFavoritesCount((recipe.getFavoritesCount() != null ? recipe.getFavoritesCount() : 0) + 1);

            userRepository.save(user);
            recipeRepository.save(recipe);
        }
    }

    public void removeRecipeFromUserSaved(String recipeId, String userId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found with id: " + recipeId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (user.getRecipesSaved().remove(recipe)) {
            recipe.setFavoritesCount((recipe.getFavoritesCount() != null ? recipe.getFavoritesCount() : 0) - 1);

            userRepository.save(user);
            recipeRepository.save(recipe);
        }
    }

    public List<Recipe> getUserSavedRecipes(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return user.getRecipesSaved().stream().toList();
    }

    public List<Recipe> getRecipesByCuisine(String cuisine) {
        return recipeRepository.findByCuisineIgnoreCase(cuisine);
    }

    public List<Recipe> searchRecipesByTitle(String title) {
        return recipeRepository.findByTitleIgnoreCase(title);
    }

    public List<Recipe> getRecipesByDietaryTag(String dietaryTag) {
        return recipeRepository.findByDietaryPreferencesContainingIgnoreCase(dietaryTag);
    }

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }
}
