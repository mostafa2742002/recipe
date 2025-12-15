package com.example.recipe.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.recipe.dto.AuthRequest;
import com.example.recipe.service.AuthService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        return authService.register(request);
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }

}
