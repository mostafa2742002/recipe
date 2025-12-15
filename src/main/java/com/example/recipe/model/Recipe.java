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


@Document(collection = "recipes")
@Data
public class Recipe {

    @Id
    private String id;

    @TextIndexed
    private String title;

    private String image;

    @Indexed
    private String cuisine; 

    @Indexed
    private Long prepTime; 

    private java.util.List<String> ingredientNames = new java.util.ArrayList<>();

    private Map<String, Integer> ingredients = new HashMap<>();

    private String steps;

    @Indexed
    private java.util.List<String> dietaryPreferences = new java.util.ArrayList<>();

    @DBRef
    private User author;

    @Indexed 
    private Integer favoritesCount = 0;

    @CreatedDate
    private Long createdAt;

    @LastModifiedDate
    private Long updatedAt;
}
