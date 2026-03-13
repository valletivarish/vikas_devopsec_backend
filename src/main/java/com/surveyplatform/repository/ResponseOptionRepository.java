package com.surveyplatform.repository;

import com.surveyplatform.model.ResponseOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// Repository for ResponseOption entity managing answer choices for multiple choice questions
@Repository
public interface ResponseOptionRepository extends JpaRepository<ResponseOption, Long> {

    // Find all options for a specific question, ordered by display order
    List<ResponseOption> findByQuestionIdOrderByOptionOrderAsc(Long questionId);

    // Delete all options for a specific question (used when updating questions)
    void deleteByQuestionId(Long questionId);
}
