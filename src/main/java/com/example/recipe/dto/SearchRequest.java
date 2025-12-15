package com.example.recipe.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Search Request DTO
 * 
 * All parameters are optional. If not provided, MongoDB ignores that filter.
 * Example: GET
 * /api/recipes/search?ingredient=chicken&diet=Keto&cuisine=Egyptian
 */
@Data
@NoArgsConstructor
public class SearchRequest {

    // Search text (searches in recipe title and ingredients)
    // Example: "pasta", "chicken biryani"
    private String searchText;

    // Filter by ingredient name
    // Example: "chicken", "tomato"
    private String ingredient;

    // Filter by cuisine type
    // Example: "Italian", "Egyptian", "Indian"
    private String cuisine;

    // Filter by dietary preference (can be multiple, but API takes one for
    // simplicity)
    // Options: "Vegan", "Vegetarian", "Keto", "Gluten-Free"
    private String dietaryPreference;

    // Filter by prep time in minutes
    // Example: 30 means show recipes with prepTime <= 30 minutes
    private Long maxPrepTime;

    // Sort by field: "relevance", "prepTime", "favorites", "createdAt"
    private String sortBy = "relevance";

    // Page number for pagination
    private Integer page = 0;

    // Items per page
    private Integer limit = 10;
}
