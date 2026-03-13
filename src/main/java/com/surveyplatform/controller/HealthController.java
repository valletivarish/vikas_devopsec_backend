package com.surveyplatform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Health check endpoint used by CI/CD pipeline smoke tests and load balancers
// Publicly accessible without authentication
@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Application health check endpoint")
public class HealthController {

    // Return application health status for monitoring and deployment verification
    @GetMapping("/health")
    @Operation(summary = "Health check endpoint for CI/CD smoke tests")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "application", "Survey Platform API"
        ));
    }
}
