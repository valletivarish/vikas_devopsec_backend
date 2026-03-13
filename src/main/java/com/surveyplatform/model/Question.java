package com.surveyplatform.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

// Question entity representing a single question within a survey
// Supports multiple choice, Likert scale, and open text question types
@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The text content of the question displayed to respondents
    @Column(nullable = false, length = 1000)
    private String text;

    // Type of question determining how responses are collected
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    // Display order of the question within the survey
    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    // Whether this question must be answered before submitting the survey
    @Column(nullable = false)
    @Builder.Default
    private Boolean required = true;

    // For Likert scale questions: minimum scale value (e.g., 1)
    @Column(name = "likert_min")
    private Integer likertMin;

    // For Likert scale questions: maximum scale value (e.g., 7 or 10)
    @Column(name = "likert_max")
    private Integer likertMax;

    // For open text questions: maximum character length allowed
    @Column(name = "max_text_length")
    private Integer maxTextLength;

    // Reference to the parent survey this question belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    // List of predefined response options for multiple choice questions
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("optionOrder ASC")
    @Builder.Default
    private List<ResponseOption> responseOptions = new ArrayList<>();

    // Optional: ID of the question that triggers conditional display of this question
    @Column(name = "conditional_question_id")
    private Long conditionalQuestionId;

    // Optional: the answer value that must match for this question to be shown
    @Column(name = "conditional_answer", length = 500)
    private String conditionalAnswer;
}
