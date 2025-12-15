package com.example.recipe.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String username; // required for registration, optional for login
    private String email; // required for both
    private String password; // required for both
}
