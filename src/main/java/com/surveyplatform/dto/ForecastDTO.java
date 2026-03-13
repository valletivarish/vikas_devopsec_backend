package com.surveyplatform.dto;

import lombok.*;
import java.util.List;

// DTO for response rate prediction results using linear regression
// Predicts survey completion rate based on survey characteristics
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForecastDTO {

    // The survey ID this forecast is for
    private Long surveyId;

    // Predicted completion rate as a percentage
    private Double predictedCompletionRate;

    // R-squared value indicating model fit quality (0-1)
    private Double confidenceScore;

    // Slope of the regression line indicating trend direction
    private Double trendSlope;

    // Trend direction: INCREASING, DECREASING, or STABLE
    private String trendDirection;

    // Historical data points used for the prediction
    private List<DataPointDTO> historicalData;

    // Predicted future data points
    private List<DataPointDTO> predictedData;

    // Inner DTO for individual data points in the forecast
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataPointDTO {
        private String label;
        private Double value;
    }
}
