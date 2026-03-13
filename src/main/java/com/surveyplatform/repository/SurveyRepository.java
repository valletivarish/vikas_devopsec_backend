package com.surveyplatform.repository;

import com.surveyplatform.model.Survey;
import com.surveyplatform.model.SurveyStatus;
import com.surveyplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

// Repository for Survey entity with custom queries for filtering and analytics
@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    // Find all surveys created by a specific user
    List<Survey> findByCreator(User creator);

    // Find all surveys created by a specific user ID
    List<Survey> findByCreatorId(Long creatorId);

    // Find surveys by their current status (DRAFT, ACTIVE, CLOSED)
    List<Survey> findByStatus(SurveyStatus status);

    // Find a survey by its unique share link identifier
    Optional<Survey> findByShareLink(String shareLink);

    // Count surveys by status for the dashboard summary
    long countByStatus(SurveyStatus status);

    // Count surveys created by a specific user
    long countByCreatorId(Long creatorId);

    // Count active surveys for a specific user
    long countByCreatorIdAndStatus(Long creatorId, SurveyStatus status);

    // Retrieve surveys with their questions eagerly loaded for display
    @Query("SELECT s FROM Survey s LEFT JOIN FETCH s.questions WHERE s.id = :id")
    Optional<Survey> findByIdWithQuestions(@Param("id") Long id);
}
