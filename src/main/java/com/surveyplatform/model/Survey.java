package com.surveyplatform.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Survey entity representing a poll or survey created by a user
// Contains metadata like title, description, visibility, and date range
@Entity
@Table(name = "surveys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Title of the survey displayed to respondents
    @Column(nullable = false, length = 200)
    private String title;

    // Detailed description explaining the purpose of the survey
    @Column(length = 2000)
    private String description;

    // Start date when the survey becomes active and accepts responses
    @Column(name = "start_date")
    private LocalDateTime startDate;

    // End date after which no more responses are accepted
    @Column(name = "end_date")
    private LocalDateTime endDate;

    // Controls whether the survey is publicly visible or restricted
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SurveyVisibility visibility = SurveyVisibility.PUBLIC;

    // Current status of the survey lifecycle
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SurveyStatus status = SurveyStatus.DRAFT;

    // The user who created this survey
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    // List of questions belonging to this survey, ordered by question order
    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("questionOrder ASC")
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    // Unique shareable link identifier for distributing the survey
    @Column(name = "share_link", unique = true, length = 50)
    private String shareLink;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
