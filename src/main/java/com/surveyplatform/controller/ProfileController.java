package com.surveyplatform.controller;

import com.surveyplatform.dto.AuthResponse;
import com.surveyplatform.dto.PasswordChangeRequest;
import com.surveyplatform.dto.ProfileUpdateRequest;
import com.surveyplatform.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile", description = "User profile management endpoints")
public class ProfileController {

    private final AuthService authService;

    public ProfileController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Get current user profile")
    public ResponseEntity<AuthResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(authService.getProfile(authentication.getName()));
    }

    @PutMapping
    @Operation(summary = "Update current user profile")
    public ResponseEntity<AuthResponse> updateProfile(Authentication authentication,
                                                       @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(authService.updateProfile(authentication.getName(), request));
    }

    @PutMapping("/password")
    @Operation(summary = "Change current user password")
    public ResponseEntity<Void> changePassword(Authentication authentication,
                                                @Valid @RequestBody PasswordChangeRequest request) {
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok().build();
    }
}
