package com.example.recipe.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.recipe.model.AuthRequest;

@RestController
public class AuthController {

    @PostMapping("/api/auth/register")
    public String register(@RequestBody AuthRequest request) {
        // Registration logic here
        return "User registered successfully";
    }

    @PostMapping("/api/auth/login")
    public String login(@RequestBody AuthRequest request) {
        // Authentication logic here
        return "User logged in successfully";
    }

}
