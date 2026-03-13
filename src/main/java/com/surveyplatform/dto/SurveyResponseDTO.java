package com.surveyplatform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

// DTO for submitting survey responses with individual answers per question
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyResponseDTO {

    private Long id;

    // ID of the survey being responded to
    @NotNull(message = "Survey ID is required")
    private Long surveyId;

    // Whether the response is complete (all required questions answered)
    private Boolean completed;

    // List of answers to individual questions
    @Valid
    @NotEmpty(message = "At least one answer is required")
    private List<AnswerDTO> answers;

    // Read-only fields populated by the server
    private String respondentUsername;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
}
