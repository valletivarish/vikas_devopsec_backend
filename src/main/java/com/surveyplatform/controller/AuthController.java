package com.surveyplatform.controller;

import com.surveyplatform.dto.AuthRequest;
import com.surveyplatform.dto.AuthResponse;
import com.surveyplatform.dto.RegisterRequest;
import com.surveyplatform.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// REST controller handling user authentication endpoints (login and registration)
// All endpoints under /api/auth are publicly accessible without JWT token
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User login and registration endpoints")
public class AuthController {

    private final AuthService authService;

    // Constructor injection for the auth service
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Register a new user account with validated input
    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Authenticate user credentials and return a JWT token
    @PostMapping("/login")
    @Operation(summary = "Login with username and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
