package com.surveyplatform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

// Health check endpoint used by CI/CD pipeline smoke tests and load balancers
// Publicly accessible without authentication
@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Application health and info endpoints")
public class HealthController {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    // Return application health status for monitoring and deployment verification
    @GetMapping("/health")
    @Operation(summary = "Health check endpoint for CI/CD smoke tests")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "application", "Survey Platform API"
        ));
    }

    @GetMapping("/info")
    @Operation(summary = "Application info endpoint")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("application", "Survey Platform API");
        info.put("version", "1.0.0");
        info.put("profile", activeProfile);
        info.put("database", "PostgreSQL (AWS RDS)");
        info.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(info);
    }
}
