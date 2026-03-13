package com.surveyplatform.service;

import com.surveyplatform.dto.DashboardDTO;
import com.surveyplatform.exception.ResourceNotFoundException;
import com.surveyplatform.model.QuestionType;
import com.surveyplatform.model.SurveyStatus;
import com.surveyplatform.model.User;
import com.surveyplatform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

// Service providing dashboard analytics data including survey counts,
// response aggregations, and question type distribution
@Service
public class DashboardService {

    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    // Constructor injection for all repository dependencies
    public DashboardService(SurveyRepository surveyRepository,
                            SurveyResponseRepository surveyResponseRepository,
                            QuestionRepository questionRepository,
                            UserRepository userRepository) {
        this.surveyRepository = surveyRepository;
        this.surveyResponseRepository = surveyResponseRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    // Build dashboard summary data for the authenticated user
    @Transactional(readOnly = true)
    public DashboardDTO getDashboard(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Long userId = user.getId();

        // Count surveys by status
        long totalSurveys = surveyRepository.countByCreatorId(userId);
        long activeSurveys = surveyRepository.countByCreatorIdAndStatus(userId, SurveyStatus.ACTIVE);
        long totalResponses = surveyResponseRepository.countByCreatorId(userId);

        // Calculate average completion rate across all user surveys
        double avgCompletionRate = 0;
        var surveys = surveyRepository.findByCreatorId(userId);
        if (!surveys.isEmpty()) {
            double totalRate = 0;
            int surveysWithResponses = 0;
            for (var survey : surveys) {
                long surveyResponses = surveyResponseRepository.countBySurveyId(survey.getId());
                if (surveyResponses > 0) {
                    long completed = surveyResponseRepository.countBySurveyIdAndCompleted(survey.getId(), true);
                    totalRate += (double) completed / surveyResponses * 100;
                    surveysWithResponses++;
                }
            }
            avgCompletionRate = surveysWithResponses > 0 ? totalRate / surveysWithResponses : 0;
        }

        // Get response counts per survey for bar chart visualization
        List<Object[]> responseCounts = surveyResponseRepository.countResponsesPerSurvey(userId);
        List<Map<String, Object>> responsesPerSurvey = new ArrayList<>();
        for (Object[] row : responseCounts) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("surveyId", row[0]);
            entry.put("title", row[1]);
            entry.put("responses", row[2]);
            responsesPerSurvey.add(entry);
        }

        // Build question type distribution for pie chart
        Map<String, Long> questionTypeDistribution = new HashMap<>();
        for (QuestionType type : QuestionType.values()) {
            questionTypeDistribution.put(type.name(), questionRepository.countByType(type));
        }

        // Build recent activity list from latest surveys
        List<Map<String, Object>> recentActivity = new ArrayList<>();
        surveys.stream()
                .sorted(Comparator.comparing(s -> s.getCreatedAt(), Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .forEach(s -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("surveyId", s.getId());
                    activity.put("title", s.getTitle());
                    activity.put("status", s.getStatus().name());
                    activity.put("createdAt", s.getCreatedAt());
                    activity.put("responses", surveyResponseRepository.countBySurveyId(s.getId()));
                    recentActivity.add(activity);
                });

        return DashboardDTO.builder()
                .totalSurveys(totalSurveys)
                .totalResponses(totalResponses)
                .activeSurveys(activeSurveys)
                .averageCompletionRate(Math.round(avgCompletionRate * 100.0) / 100.0)
                .responsesPerSurvey(responsesPerSurvey)
                .recentActivity(recentActivity)
                .questionTypeDistribution(questionTypeDistribution)
                .build();
    }
}
