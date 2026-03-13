package com.surveyplatform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

// DTO for creating and updating surveys with full validation
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyDTO {

    private Long id;

    // Survey title is required and limited to 200 characters
    @NotBlank(message = "Survey title is required")
    @Size(max = 200, message = "Survey title must not exceed 200 characters")
    private String title;

    // Optional description limited to 2000 characters
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    // Start date must be provided for scheduling the survey
    private LocalDateTime startDate;

    // End date must be after the start date
    private LocalDateTime endDate;

    // Visibility setting: PUBLIC or PRIVATE
    private String visibility;

    // Current status: DRAFT, ACTIVE, or CLOSED
    private String status;

    // Nested list of questions with cascading validation
    @Valid
    @Size(min = 1, message = "Survey must have at least 1 question")
    private List<QuestionDTO> questions;

    // Read-only fields populated by the server
    private String creatorUsername;
    private String shareLink;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer totalResponses;
}
