package com.surveyplatform.controller;

import com.surveyplatform.dto.SurveyResponseDTO;
import com.surveyplatform.service.SurveyResponseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// REST controller for managing survey response submissions
// Supports both authenticated and anonymous survey responses
@RestController
@RequestMapping("/api/responses")
@Tag(name = "Survey Responses", description = "Survey response submission and retrieval endpoints")
public class SurveyResponseController {

    private final SurveyResponseService surveyResponseService;

    // Constructor injection for the response service
    public SurveyResponseController(SurveyResponseService surveyResponseService) {
        this.surveyResponseService = surveyResponseService;
    }

    // Submit a response to a survey (authenticated users)
    @PostMapping
    @Operation(summary = "Submit a survey response")
    public ResponseEntity<SurveyResponseDTO> submitResponse(@Valid @RequestBody SurveyResponseDTO responseDTO,
                                                             Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        SurveyResponseDTO created = surveyResponseService.submitResponse(responseDTO, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Submit a response via public survey share link (no auth required)
    @PostMapping("/surveys/{surveyId}/respond")
    @Operation(summary = "Submit a response to a survey via share link (public)")
    public ResponseEntity<SurveyResponseDTO> submitPublicResponse(@PathVariable Long surveyId,
                                                                    @Valid @RequestBody SurveyResponseDTO responseDTO) {
        responseDTO.setSurveyId(surveyId);
        SurveyResponseDTO created = surveyResponseService.submitResponse(responseDTO, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Get all responses for a specific survey (only the survey creator can view)
    @GetMapping("/survey/{surveyId}")
    @Operation(summary = "Get all responses for a survey")
    public ResponseEntity<List<SurveyResponseDTO>> getResponsesBySurvey(@PathVariable Long surveyId,
                                                                          Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(surveyResponseService.getResponsesBySurvey(surveyId, username));
    }

    // Get survey IDs the current user has already responded to
    @GetMapping("/my-responded-surveys")
    @Operation(summary = "Get survey IDs the current user has responded to")
    public ResponseEntity<List<Long>> getMyRespondedSurveyIds(Authentication authentication) {
        return ResponseEntity.ok(surveyResponseService.getRespondedSurveyIds(authentication.getName()));
    }

    // Get a specific response by ID
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Get a survey response by ID")
    public ResponseEntity<SurveyResponseDTO> getResponseById(@PathVariable Long id) {
        return ResponseEntity.ok(surveyResponseService.getResponseById(id));
    }
}
