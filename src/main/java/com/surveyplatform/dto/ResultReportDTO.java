package com.surveyplatform.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

// DTO for creating and viewing result reports generated from survey responses
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultReportDTO {

    private Long id;

    // ID of the survey this report is for
    @NotNull(message = "Survey ID is required")
    private Long surveyId;

    // Report title for identification
    @NotBlank(message = "Report title is required")
    @Size(max = 200, message = "Report title must not exceed 200 characters")
    private String title;

    // JSON summary data with response statistics
    private String summaryData;

    // Total number of responses included
    private Integer totalResponses;

    // Percentage of completed responses
    private Double completionRate;

    // Average completion time in seconds
    private Long averageTimeSeconds;

    // Username of the report generator
    private String generatedByUsername;

    private LocalDateTime createdAt;
}
