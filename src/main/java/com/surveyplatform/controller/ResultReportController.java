package com.surveyplatform.controller;

import com.surveyplatform.dto.ResultReportDTO;
import com.surveyplatform.service.ResultReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// REST controller for generating and managing survey result reports
// Reports contain aggregated analytics including completion rates and answer distributions
@RestController
@RequestMapping("/api/reports")
@Validated
@Tag(name = "Result Reports", description = "Survey result report generation and management")
public class ResultReportController {

    private final ResultReportService resultReportService;

    // Constructor injection for the report service
    public ResultReportController(ResultReportService resultReportService) {
        this.resultReportService = resultReportService;
    }

    // Generate a new result report for a survey
    @PostMapping("/survey/{surveyId}")
    @Operation(summary = "Generate a result report for a survey")
    public ResponseEntity<ResultReportDTO> generateReport(@PathVariable Long surveyId,
                                                           @RequestParam @NotBlank(message = "Report title is required") String title,
                                                           Authentication authentication) {
        ResultReportDTO report = resultReportService.generateReport(surveyId, title, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    // Get all reports for a specific survey
    @GetMapping("/survey/{surveyId}")
    @Operation(summary = "Get all reports for a survey")
    public ResponseEntity<List<ResultReportDTO>> getReportsBySurvey(@PathVariable Long surveyId) {
        return ResponseEntity.ok(resultReportService.getReportsBySurvey(surveyId));
    }

    // Get a specific report by ID
    @GetMapping("/{id}")
    @Operation(summary = "Get a report by ID")
    public ResponseEntity<ResultReportDTO> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(resultReportService.getReportById(id));
    }

    // Delete a result report
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a result report")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        resultReportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}
