package com.example.recipe.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.recipe.config.JwtService;
import com.example.recipe.dto.ApiResponse;
import com.example.recipe.dto.AuthRequest;
import com.example.recipe.model.Role;
import com.example.recipe.model.User;
import com.example.recipe.repository.RoleRepository;
import com.example.recipe.repository.UserRepository;

import lombok.AllArgsConstructor;
import java.util.Map;

@Service
@AllArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<ApiResponse<Map<String, String>>> register(@RequestBody AuthRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            ApiResponse<Map<String, String>> response = ApiResponse.failure("Email is already in use!",
                    HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("ROLE_USER");
                    return roleRepository.save(newRole);
                });

        user.getRoles().add(userRole);
        userRepository.save(user);
        String jwtToken = jwtService.generateToken(user.getEmail());
        Map<String, String> payload = Map.of("token", jwtToken);
        ApiResponse<Map<String, String>> response = ApiResponse.success("User registered successfully", payload,
                HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<ApiResponse<Map<String, String>>> login(@RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));

            String jwtToken = jwtService.generateToken(authentication.getName());
            Map<String, String> payload = Map.of("token", jwtToken);
            ApiResponse<Map<String, String>> response = ApiResponse.success("User logged in successfully", payload,
                    HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (AuthenticationException ex) {
            ApiResponse<Map<String, String>> response = ApiResponse.failure("Invalid email or password",
                    HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

}
