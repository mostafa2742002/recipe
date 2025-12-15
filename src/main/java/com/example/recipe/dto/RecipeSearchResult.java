package com.example.recipe.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeSearchResult {
    private String id;
    private String title;
    private String image;
    private String cuisine;
    private Long prepTime;
    private List<String> ingredientNames;
    private List<String> dietaryPreferences;
    private String authorName;
    private Integer favoritesCount;
    private Double relevanceScore;
}
