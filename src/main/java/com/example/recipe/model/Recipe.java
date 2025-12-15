package com.example.recipe.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "recipes")
@Data
public class Recipe {

    @Id
    private String id;

    private String title;
    private String image;
    private String cuisine;
    private Long prepTime;
    private Map<String, Integer> ingredients = new HashMap<>();
    private String steps;
    private String dietaryTags;

    @DBRef // reference to the user who authored this recipe
    private User author;

    private Integer favoritesCount = 0;

    @CreatedDate
    private Long createdAt;

    @LastModifiedDate
    private Long updatedAt;
}
