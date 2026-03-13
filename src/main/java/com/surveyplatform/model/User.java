package com.surveyplatform.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// User entity representing registered users who can create and respond to surveys
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique username for login
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // Email address used for account recovery and notifications
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // Encrypted password stored using BCrypt hashing
    @Column(nullable = false)
    private String password;

    // Full display name shown on surveys and responses
    @Column(name = "full_name", length = 100)
    private String fullName;

    // Role-based access control: ADMIN or USER
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    // Timestamp when the user account was created
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
