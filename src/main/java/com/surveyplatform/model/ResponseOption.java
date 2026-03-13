package com.surveyplatform.model;

import jakarta.persistence.*;
import lombok.*;

// ResponseOption entity representing a selectable answer choice for multiple choice questions
@Entity
@Table(name = "response_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The text displayed for this option
    @Column(nullable = false, length = 500)
    private String text;

    // Display order of this option within the question
    @Column(name = "option_order", nullable = false)
    private Integer optionOrder;

    // Reference to the parent question this option belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
}
