package com.example.recipe.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * Recipe document for MongoDB.
 * 
 * Indexes:
 * - @TextIndexed on title & ingredients: enables full-text search
 * - @Indexed on cuisine, dietaryTags, prepTime: speeds up filtering
 */
@Document(collection = "recipes")
@Data
public class Recipe {

    @Id
    private String id;

    // Full-text searchable fields
    @TextIndexed
    private String title;

    private String image;

    // Indexed for fast filtering
    @Indexed
    private String cuisine; // Italian, Egyptian, Indian, etc.

    @Indexed
    private Long prepTime; // in minutes

    // Ingredients stored as list for easier searching
    private java.util.List<String> ingredientNames = new java.util.ArrayList<>();

    // Original map kept for quantity info
    private Map<String, Integer> ingredients = new HashMap<>();

    private String steps;

    // Indexed for dietary filters (Vegan, Vegetarian, Keto, Gluten-Free)
    @Indexed
    private java.util.List<String> dietaryPreferences = new java.util.ArrayList<>();

    @DBRef
    private User author;

    @Indexed // Index favorites for sorting popular recipes
    private Integer favoritesCount = 0;

    @CreatedDate
    private Long createdAt;

    @LastModifiedDate
    private Long updatedAt;
}
