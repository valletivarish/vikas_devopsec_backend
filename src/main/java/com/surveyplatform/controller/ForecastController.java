package com.surveyplatform.controller;

import com.surveyplatform.dto.ForecastDTO;
import com.surveyplatform.service.ForecastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// REST controller for ML-based response rate forecasting
// Uses linear regression to predict survey completion trends
@RestController
@RequestMapping("/api/forecast")
@Tag(name = "Forecast", description = "Response rate prediction and trend analysis endpoints")
public class ForecastController {

    private final ForecastService forecastService;

    // Constructor injection for the forecast service
    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    // Generate a response rate forecast for a specific survey
    @GetMapping("/survey/{surveyId}")
    @Operation(summary = "Get response rate forecast for a survey")
    public ResponseEntity<ForecastDTO> getForecast(@PathVariable Long surveyId) {
        return ResponseEntity.ok(forecastService.generateForecast(surveyId));
    }

    // Generate forecasts for all surveys with sufficient data
    @GetMapping("/all")
    @Operation(summary = "Get forecasts for all surveys")
    public ResponseEntity<List<ForecastDTO>> getAllForecasts(Authentication authentication) {
        return ResponseEntity.ok(forecastService.generateAllForecasts(authentication.getName()));
    }
}
