package com.surveyplatform.repository;

import com.surveyplatform.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

// Repository for Answer entity with aggregation queries for analytics
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    // Find all answers for a specific survey response
    List<Answer> findBySurveyResponseId(Long surveyResponseId);

    // Find all answers for a specific question across all responses
    List<Answer> findByQuestionId(Long questionId);

    // Count answers grouped by selected option for multiple choice analytics
    @Query("SELECT a.selectedOptionId, COUNT(a) FROM Answer a WHERE a.question.id = :questionId " +
           "AND a.selectedOptionId IS NOT NULL GROUP BY a.selectedOptionId")
    List<Object[]> countBySelectedOption(@Param("questionId") Long questionId);

    // Get average Likert value for a question
    @Query("SELECT AVG(CAST(a.answerValue AS double)) FROM Answer a WHERE a.question.id = :questionId " +
           "AND a.answerValue IS NOT NULL")
    Double averageLikertValue(@Param("questionId") Long questionId);
}
