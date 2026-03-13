package com.surveyplatform.repository;

import com.surveyplatform.model.Question;
import com.surveyplatform.model.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// Repository for Question entity with queries for survey-specific operations
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // Find all questions belonging to a specific survey, ordered by display order
    List<Question> findBySurveyIdOrderByQuestionOrderAsc(Long surveyId);

    // Count questions in a survey to validate minimum question requirement
    long countBySurveyId(Long surveyId);

    // Count questions by type across all surveys for analytics
    long countByType(QuestionType type);

    // Delete all questions for a specific survey (used in survey updates)
    void deleteBySurveyId(Long surveyId);
}
