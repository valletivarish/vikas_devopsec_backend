package com.surveyplatform.dto;

import jakarta.validation.constraints.*;
import lombok.*;

// DTO for user registration with validation rules for all required fields
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    // Username must be between 3 and 50 characters
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    // Email must be a valid email format
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    // Password must be at least 8 characters for security
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    // Full name displayed on the user profile
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;
}
