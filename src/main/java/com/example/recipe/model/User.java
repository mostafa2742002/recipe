package com.example.recipe.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "users")
@Data
public class User {
    @Id
    private String id;

    private String username;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String bio;

    private String profileImage;

    @DBRef 
    private Set<Recipe> recipesAuthored = new HashSet<>();

    @DBRef 
    private Set<Recipe> recipesSaved = new HashSet<>();

    private Boolean enabled = true;

    @DBRef 
    private Set<Role> roles = new HashSet<>();

}
