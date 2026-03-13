package com.surveyplatform.model;

import jakarta.persistence.*;
import lombok.*;

// Answer entity representing a respondent's answer to a specific question
// Stores the answer value as text regardless of question type for flexibility
@Entity
@Table(name = "answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference to the survey response this answer belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_response_id", nullable = false)
    private SurveyResponse surveyResponse;

    // Reference to the question being answered
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // The answer value: option ID for multiple choice, number for Likert, text for open text
    @Column(name = "answer_value", length = 5000)
    private String answerValue;

    // For multiple choice: the selected option ID
    @Column(name = "selected_option_id")
    private Long selectedOptionId;
}
