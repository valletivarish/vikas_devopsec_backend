package com.surveyplatform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

// DTO for creating and updating questions within a survey
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDTO {

    private Long id;

    // Question text is required and limited to 1000 characters
    @NotBlank(message = "Question text is required")
    @Size(max = 1000, message = "Question text must not exceed 1000 characters")
    private String text;

    // Question type: MULTIPLE_CHOICE, LIKERT, or OPEN_TEXT
    @NotBlank(message = "Question type is required")
    private String type;

    // Display order within the survey (positive integer)
    @NotNull(message = "Question order is required")
    @Min(value = 1, message = "Question order must be at least 1")
    private Integer questionOrder;

    // Whether the question must be answered
    private Boolean required;

    // Likert scale minimum value (1-7 or 1-10 range start)
    @Min(value = 1, message = "Likert minimum must be at least 1")
    @Max(value = 10, message = "Likert minimum must not exceed 10")
    private Integer likertMin;

    // Likert scale maximum value (1-7 or 1-10 range end)
    @Min(value = 1, message = "Likert maximum must be at least 1")
    @Max(value = 10, message = "Likert maximum must not exceed 10")
    private Integer likertMax;

    // Maximum length for open text responses (configurable, max 5000)
    @Max(value = 5000, message = "Max text length must not exceed 5000")
    private Integer maxTextLength;

    // Predefined response options for multiple choice questions
    @Valid
    private List<ResponseOptionDTO> responseOptions;

    // Conditional logic: show question only when specific answer is given
    private Long conditionalQuestionId;
    private String conditionalAnswer;
}
