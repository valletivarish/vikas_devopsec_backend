package com.surveyplatform.dto;

import jakarta.validation.constraints.*;
import lombok.*;

// DTO for creating and updating response options for multiple choice questions
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseOptionDTO {

    private Long id;

    // Option text is required and limited to 500 characters
    @NotBlank(message = "Option text is required")
    @Size(max = 500, message = "Option text must not exceed 500 characters")
    private String text;

    // Display order of this option within the question
    @NotNull(message = "Option order is required")
    @Min(value = 1, message = "Option order must be at least 1")
    private Integer optionOrder;
}
