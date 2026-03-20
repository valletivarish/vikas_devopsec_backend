package com.surveyplatform.service;

import com.surveyplatform.dto.ForecastDTO;
import com.surveyplatform.exception.BadRequestException;
import com.surveyplatform.exception.ResourceNotFoundException;
import com.surveyplatform.model.Survey;
import com.surveyplatform.repository.SurveyRepository;
import com.surveyplatform.repository.SurveyResponseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

// Unit tests for ForecastService verifying linear regression prediction logic
@ExtendWith(MockitoExtension.class)
class ForecastServiceTest {

    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private SurveyResponseRepository surveyResponseRepository;

    @InjectMocks
    private ForecastService forecastService;

    // Test forecast generation with sufficient data produces valid predictions
    @Test
    void generateForecastShouldReturnPredictions() {
        Survey survey = Survey.builder().id(1L).build();
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(survey));

        // Simulate daily response counts over 5 days
        List<Object[]> dailyCounts = Arrays.asList(
                new Object[]{Date.valueOf("2024-01-01"), 5L},
                new Object[]{Date.valueOf("2024-01-02"), 8L},
                new Object[]{Date.valueOf("2024-01-03"), 12L},
                new Object[]{Date.valueOf("2024-01-04"), 15L},
                new Object[]{Date.valueOf("2024-01-05"), 18L}
        );

        when(surveyResponseRepository.countResponsesByDay(1L)).thenReturn(dailyCounts);
        when(surveyResponseRepository.countBySurveyId(1L)).thenReturn(58L);
        when(surveyResponseRepository.countBySurveyIdAndCompleted(1L, true)).thenReturn(50L);

        ForecastDTO result = forecastService.generateForecast(1L);

        assertNotNull(result);
        assertEquals(1L, result.getSurveyId());
        assertEquals("INCREASING", result.getTrendDirection());
        assertTrue(result.getTrendSlope() > 0);
        assertEquals(5, result.getHistoricalData().size());
        assertEquals(7, result.getPredictedData().size());
    }

    // Test forecast with no data throws BadRequestException
    @Test
    void generateForecastShouldThrowWhenNoData() {
        Survey survey = Survey.builder().id(1L).build();
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(survey));
        when(surveyResponseRepository.countResponsesByDay(1L)).thenReturn(Collections.emptyList());

        assertThrows(BadRequestException.class, () -> forecastService.generateForecast(1L));
    }

    // Test forecast for nonexistent survey throws exception
    @Test
    void generateForecastShouldThrowForMissingSurvey() {
        when(surveyRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> forecastService.generateForecast(999L));
    }

    // Test forecast with decreasing trend
    @Test
    void generateForecastShouldDetectDecreasingTrend() {
        Survey survey = Survey.builder().id(1L).build();
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(survey));

        List<Object[]> dailyCounts = Arrays.asList(
                new Object[]{Date.valueOf("2024-01-01"), 20L},
                new Object[]{Date.valueOf("2024-01-02"), 15L},
                new Object[]{Date.valueOf("2024-01-03"), 10L},
                new Object[]{Date.valueOf("2024-01-04"), 5L},
                new Object[]{Date.valueOf("2024-01-05"), 1L}
        );

        when(surveyResponseRepository.countResponsesByDay(1L)).thenReturn(dailyCounts);
        when(surveyResponseRepository.countBySurveyId(1L)).thenReturn(51L);
        when(surveyResponseRepository.countBySurveyIdAndCompleted(1L, true)).thenReturn(40L);

        ForecastDTO result = forecastService.generateForecast(1L);

        assertEquals("DECREASING", result.getTrendDirection());
        assertTrue(result.getTrendSlope() < 0);
    }

    // Test generateAllForecasts returns list and skips surveys without data
    @Test
    void generateAllForecastsShouldSkipSurveysWithoutData() {
        Survey s1 = Survey.builder().id(1L).build();
        Survey s2 = Survey.builder().id(2L).build();
        when(surveyRepository.findAll()).thenReturn(Arrays.asList(s1, s2));

        // s1 has data, s2 does not
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(s1));
        List<Object[]> dailyCounts = Arrays.asList(
                new Object[]{Date.valueOf("2024-01-01"), 5L},
                new Object[]{Date.valueOf("2024-01-02"), 5L}
        );
        when(surveyResponseRepository.countResponsesByDay(1L)).thenReturn(dailyCounts);
        when(surveyResponseRepository.countBySurveyId(1L)).thenReturn(10L);
        when(surveyResponseRepository.countBySurveyIdAndCompleted(1L, true)).thenReturn(8L);

        when(surveyRepository.findById(2L)).thenReturn(Optional.of(s2));
        when(surveyResponseRepository.countResponsesByDay(2L)).thenReturn(Collections.emptyList());

        List<ForecastDTO> results = forecastService.generateAllForecasts("testuser");

        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getSurveyId());
    }
}
