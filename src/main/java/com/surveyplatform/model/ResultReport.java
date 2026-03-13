package com.surveyplatform.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// ResultReport entity representing a generated analysis report for a survey
// Contains summary statistics, charts data, and completion metrics
@Entity
@Table(name = "result_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The survey this report is generated for
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    // Title of the report for identification
    @Column(nullable = false, length = 200)
    private String title;

    // JSON-formatted summary data containing response statistics
    @Column(name = "summary_data", columnDefinition = "TEXT")
    private String summaryData;

    // Total number of responses included in this report
    @Column(name = "total_responses")
    private Integer totalResponses;

    // Percentage of respondents who completed all required questions
    @Column(name = "completion_rate")
    private Double completionRate;

    // Average time in seconds taken to complete the survey
    @Column(name = "average_time_seconds")
    private Long averageTimeSeconds;

    // The user who generated this report
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by")
    private User generatedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
