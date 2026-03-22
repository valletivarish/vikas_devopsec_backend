package com.surveyplatform.controller;

import com.surveyplatform.dto.SurveyDTO;
import com.surveyplatform.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// REST controller managing full CRUD operations for surveys
// Supports creating surveys with nested questions and response options
@RestController
@RequestMapping("/api/surveys")
@Tag(name = "Surveys", description = "Survey CRUD and management endpoints")
public class SurveyController {

    private final SurveyService surveyService;

    // Constructor injection for the survey service
    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    // Create a new survey with questions and response options
    @PostMapping
    @Operation(summary = "Create a new survey with questions")
    public ResponseEntity<SurveyDTO> createSurvey(@Valid @RequestBody SurveyDTO surveyDTO,
                                                    Authentication authentication) {
        SurveyDTO created = surveyService.createSurvey(surveyDTO, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Get all surveys created by the authenticated user
    @GetMapping
    @Operation(summary = "Get all surveys for the authenticated user")
    public ResponseEntity<List<SurveyDTO>> getMySurveys(Authentication authentication) {
        List<SurveyDTO> surveys = surveyService.getSurveysByUser(authentication.getName());
        return ResponseEntity.ok(surveys);
    }

    // Get all surveys (admin/overview functionality)
    @GetMapping("/all")
    @Operation(summary = "Get all surveys in the platform")
    public ResponseEntity<List<SurveyDTO>> getAllSurveys() {
        return ResponseEntity.ok(surveyService.getAllSurveys());
    }

    // Get a specific survey by its ID
    @GetMapping("/{id}")
    @Operation(summary = "Get a survey by ID")
    public ResponseEntity<SurveyDTO> getSurveyById(@PathVariable Long id) {
        return ResponseEntity.ok(surveyService.getSurveyById(id));
    }

    // Get a survey by its unique share link for public access
    @GetMapping("/share/{shareLink}")
    @Operation(summary = "Get a survey by share link (public access)")
    public ResponseEntity<SurveyDTO> getSurveyByShareLink(@PathVariable String shareLink) {
        return ResponseEntity.ok(surveyService.getSurveyByShareLink(shareLink));
    }

    // Update an existing survey (only by the creator)
    @PutMapping("/{id}")
    @Operation(summary = "Update a survey")
    public ResponseEntity<SurveyDTO> updateSurvey(@PathVariable Long id,
                                                    @Valid @RequestBody SurveyDTO surveyDTO,
                                                    Authentication authentication) {
        SurveyDTO updated = surveyService.updateSurvey(id, surveyDTO, authentication.getName());
        return ResponseEntity.ok(updated);
    }

    // Update survey status (DRAFT, ACTIVE, CLOSED)
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update survey status")
    public ResponseEntity<SurveyDTO> updateStatus(@PathVariable Long id,
                                                   @RequestParam String status,
                                                   Authentication authentication) {
        SurveyDTO updated = surveyService.updateStatus(id, status, authentication.getName());
        return ResponseEntity.ok(updated);
    }

    // Toggle survey visibility (PUBLIC ↔ PRIVATE)
    @PatchMapping("/{id}/visibility")
    @Operation(summary = "Toggle survey visibility")
    public ResponseEntity<SurveyDTO> toggleVisibility(@PathVariable Long id,
                                                       Authentication authentication) {
        SurveyDTO updated = surveyService.toggleVisibility(id, authentication.getName());
        return ResponseEntity.ok(updated);
    }

    // Delete a survey and all associated data (only by the creator)
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a survey")
    public ResponseEntity<Void> deleteSurvey(@PathVariable Long id,
                                              Authentication authentication) {
        surveyService.deleteSurvey(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
