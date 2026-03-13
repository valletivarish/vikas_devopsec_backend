package com.surveyplatform.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

// DTO for the dashboard overview containing summary cards and chart data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {

    // Total number of surveys created by the user
    private Long totalSurveys;

    // Total number of responses received across all surveys
    private Long totalResponses;

    // Number of currently active surveys
    private Long activeSurveys;

    // Average completion rate across all surveys
    private Double averageCompletionRate;

    // Response counts grouped by survey for chart visualization
    private List<Map<String, Object>> responsesPerSurvey;

    // Recent survey activity with timestamps
    private List<Map<String, Object>> recentActivity;

    // Question type distribution across all surveys
    private Map<String, Long> questionTypeDistribution;
}
