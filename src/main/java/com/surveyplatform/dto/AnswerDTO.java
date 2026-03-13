package com.surveyplatform.dto;

import jakarta.validation.constraints.*;
import lombok.*;

// DTO for individual question answers within a survey response
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerDTO {

    private Long id;

    // ID of the question being answered
    @NotNull(message = "Question ID is required")
    private Long questionId;

    // The answer value: text for open text, number for Likert, option text for multiple choice
    @Size(max = 5000, message = "Answer value must not exceed 5000 characters")
    private String answerValue;

    // For multiple choice: ID of the selected response option
    private Long selectedOptionId;
}
