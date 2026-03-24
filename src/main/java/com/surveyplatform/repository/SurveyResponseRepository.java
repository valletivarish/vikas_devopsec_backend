package com.surveyplatform.repository;

import com.surveyplatform.model.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

// Repository for SurveyResponse entity with analytics and aggregation queries
@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {

    // Find all responses for a specific survey
    List<SurveyResponse> findBySurveyId(Long surveyId);

    // Delete all responses for a specific survey
    void deleteBySurveyId(Long surveyId);

    // Find all responses submitted by a specific user
    List<SurveyResponse> findByRespondentId(Long respondentId);

    // Count total responses for a specific survey
    long countBySurveyId(Long surveyId);

    // Count completed responses for a specific survey
    long countBySurveyIdAndCompleted(Long surveyId, Boolean completed);

    // Count total responses across all surveys for a specific creator
    @Query("SELECT COUNT(sr) FROM SurveyResponse sr WHERE sr.survey.creator.id = :creatorId")
    long countByCreatorId(@Param("creatorId") Long creatorId);

    // Count responses submitted within a date range for trend analysis
    @Query("SELECT COUNT(sr) FROM SurveyResponse sr WHERE sr.survey.id = :surveyId AND sr.submittedAt BETWEEN :start AND :end")
    long countBySurveyIdAndSubmittedAtBetween(@Param("surveyId") Long surveyId,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);

    // Get response counts grouped by day for forecasting
    @Query("SELECT CAST(sr.submittedAt AS date) as day, COUNT(sr) as count " +
           "FROM SurveyResponse sr WHERE sr.survey.id = :surveyId AND sr.submittedAt IS NOT NULL " +
           "GROUP BY CAST(sr.submittedAt AS date) ORDER BY day")
    List<Object[]> countResponsesByDay(@Param("surveyId") Long surveyId);

    // Get survey IDs that a specific user has already responded to
    @Query("SELECT DISTINCT sr.survey.id FROM SurveyResponse sr WHERE sr.respondent.username = :username")
    List<Long> findRespondedSurveyIdsByUsername(@Param("username") String username);

    // Get all response counts per survey for dashboard chart
    @Query("SELECT sr.survey.id, sr.survey.title, COUNT(sr) FROM SurveyResponse sr " +
           "WHERE sr.survey.creator.id = :creatorId GROUP BY sr.survey.id, sr.survey.title")
    List<Object[]> countResponsesPerSurvey(@Param("creatorId") Long creatorId);
}
