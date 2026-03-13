package com.surveyplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

// DTO for login authentication requests containing username and password
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    // Username is required and cannot be blank for authentication
    @NotBlank(message = "Username is required")
    private String username;

    // Password is required and cannot be blank for authentication
    @NotBlank(message = "Password is required")
    private String password;
}
