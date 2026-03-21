package com.surveyplatform.dto;

import lombok.*;
import java.time.LocalDateTime;

// DTO returned after successful authentication containing the JWT token and user details
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private LocalDateTime createdAt;
}
