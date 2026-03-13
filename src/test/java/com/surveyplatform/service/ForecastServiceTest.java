package com.surveyplatform.service;

import com.surveyplatform.dto.ForecastDTO;
import com.surveyplatform.exception.BadRequestException;
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
}
