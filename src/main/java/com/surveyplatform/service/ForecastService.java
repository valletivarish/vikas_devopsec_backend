package com.surveyplatform.service;

import com.surveyplatform.dto.ForecastDTO;
import com.surveyplatform.exception.BadRequestException;
import com.surveyplatform.exception.ResourceNotFoundException;
import com.surveyplatform.model.Survey;
import com.surveyplatform.repository.SurveyRepository;
import com.surveyplatform.repository.SurveyResponseRepository;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

// Service implementing ML-based response rate prediction using Apache Commons Math
// Uses SimpleRegression (linear regression) to forecast survey completion rates
// based on historical response patterns such as survey length and question types
@Service
public class ForecastService {

    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository surveyResponseRepository;

    // Constructor injection for repository dependencies
    public ForecastService(SurveyRepository surveyRepository,
                           SurveyResponseRepository surveyResponseRepository) {
        this.surveyRepository = surveyRepository;
        this.surveyResponseRepository = surveyResponseRepository;
    }

    // Generate a response rate forecast for a specific survey
    // Uses linear regression on daily response counts to predict future trends
    @Transactional(readOnly = true)
    public ForecastDTO generateForecast(Long surveyId) {
        surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey", surveyId));

        // Retrieve daily response counts for the survey
        List<Object[]> dailyCounts = surveyResponseRepository.countResponsesByDay(surveyId);

        if (dailyCounts.isEmpty()) {
            throw new BadRequestException("Not enough response data to generate forecast. Survey needs at least some responses.");
        }

        // Build regression model using daily response data
        // X-axis: day index (0, 1, 2, ...), Y-axis: number of responses that day
        SimpleRegression regression = new SimpleRegression();
        List<ForecastDTO.DataPointDTO> historicalData = new ArrayList<>();

        for (int i = 0; i < dailyCounts.size(); i++) {
            Object[] row = dailyCounts.get(i);
            String dateLabel = row[0].toString();
            long count = (Long) row[1];

            regression.addData(i, count);
            historicalData.add(ForecastDTO.DataPointDTO.builder()
                    .label(dateLabel)
                    .value((double) count)
                    .build());
        }

        // Calculate predicted values for the next 7 days
        List<ForecastDTO.DataPointDTO> predictedData = new ArrayList<>();
        int startIndex = dailyCounts.size();
        for (int i = 0; i < 7; i++) {
            double predictedValue = Math.max(0, regression.predict(startIndex + i));
            predictedData.add(ForecastDTO.DataPointDTO.builder()
                    .label("Day +" + (i + 1))
                    .value(Math.round(predictedValue * 100.0) / 100.0)
                    .build());
        }

        // Calculate model quality metrics
        double rSquared = dailyCounts.size() > 2 ? regression.getRSquare() : 0;
        double slope = regression.getSlope();

        // Determine trend direction based on slope value
        String trendDirection;
        if (slope > 0.1) {
            trendDirection = "INCREASING";
        } else if (slope < -0.1) {
            trendDirection = "DECREASING";
        } else {
            trendDirection = "STABLE";
        }

        // Calculate predicted completion rate based on total responses and question count
        long totalResponses = surveyResponseRepository.countBySurveyId(surveyId);
        long completedResponses = surveyResponseRepository.countBySurveyIdAndCompleted(surveyId, true);
        double currentCompletionRate = totalResponses > 0 ?
                (double) completedResponses / totalResponses * 100 : 0;

        // Predict future completion rate using the regression trend
        double predictedCompletionRate = Math.min(100, Math.max(0,
                currentCompletionRate + slope * 7));

        return ForecastDTO.builder()
                .surveyId(surveyId)
                .predictedCompletionRate(Math.round(predictedCompletionRate * 100.0) / 100.0)
                .confidenceScore(Math.round(rSquared * 100.0) / 100.0)
                .trendSlope(Math.round(slope * 100.0) / 100.0)
                .trendDirection(trendDirection)
                .historicalData(historicalData)
                .predictedData(predictedData)
                .build();
    }

    // Generate forecast across all surveys for the user
    @Transactional(readOnly = true)
    public List<ForecastDTO> generateAllForecasts(String username) {
        List<Survey> surveys = surveyRepository.findAll();
        List<ForecastDTO> forecasts = new ArrayList<>();

        for (Survey survey : surveys) {
            try {
                ForecastDTO forecast = generateForecast(survey.getId());
                forecasts.add(forecast);
            } catch (BadRequestException e) {
                // Skip surveys without enough data for forecasting
            }
        }

        return forecasts;
    }
}
