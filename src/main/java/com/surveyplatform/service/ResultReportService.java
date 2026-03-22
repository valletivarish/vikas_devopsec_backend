package com.surveyplatform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surveyplatform.dto.ResultReportDTO;
import com.surveyplatform.exception.ResourceNotFoundException;
import com.surveyplatform.model.*;
import com.surveyplatform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

// Service managing result report generation and retrieval
// Generates summary statistics including completion rates and average response times
@Service
public class ResultReportService {

    private final ResultReportRepository resultReportRepository;
    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // Constructor injection for all dependencies
    public ResultReportService(ResultReportRepository resultReportRepository,
                                SurveyRepository surveyRepository,
                                SurveyResponseRepository surveyResponseRepository,
                                AnswerRepository answerRepository,
                                UserRepository userRepository,
                                ObjectMapper objectMapper) {
        this.resultReportRepository = resultReportRepository;
        this.surveyRepository = surveyRepository;
        this.surveyResponseRepository = surveyResponseRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    // Generate a new result report from current survey response data
    @Transactional
    public ResultReportDTO generateReport(Long surveyId, String title, String username) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey", surveyId));

        User generatedBy = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<SurveyResponse> responses = surveyResponseRepository.findBySurveyId(surveyId);

        // Calculate completion rate from total responses
        long totalResponses = responses.size();
        long completedResponses = responses.stream().filter(SurveyResponse::getCompleted).count();
        double completionRate = totalResponses > 0 ? (double) completedResponses / totalResponses * 100 : 0;

        // Calculate average response time in seconds (use absolute value and filter out zero durations)
        long avgTimeSeconds = 0;
        List<Long> durations = responses.stream()
                .filter(r -> r.getStartedAt() != null && r.getSubmittedAt() != null)
                .map(r -> Math.abs(Duration.between(r.getStartedAt(), r.getSubmittedAt()).getSeconds()))
                .filter(d -> d > 0)
                .collect(Collectors.toList());
        if (!durations.isEmpty()) {
            avgTimeSeconds = durations.stream().mapToLong(Long::longValue).sum() / durations.size();
        }

        // Build summary data JSON with per-question analytics
        Map<String, Object> summaryMap = new HashMap<>();
        summaryMap.put("totalResponses", totalResponses);
        summaryMap.put("completedResponses", completedResponses);
        summaryMap.put("completionRate", completionRate);
        summaryMap.put("averageTimeSeconds", avgTimeSeconds);

        // Per-question analytics
        List<Map<String, Object>> questionAnalytics = new ArrayList<>();
        for (Question question : survey.getQuestions()) {
            Map<String, Object> qAnalytics = new HashMap<>();
            qAnalytics.put("questionId", question.getId());
            qAnalytics.put("questionText", question.getText());
            qAnalytics.put("type", question.getType().name());

            if (question.getType() == QuestionType.MULTIPLE_CHOICE) {
                List<Object[]> optionCounts = answerRepository.countBySelectedOption(question.getId());
                Map<Long, Long> optionMap = new HashMap<>();
                for (Object[] row : optionCounts) {
                    optionMap.put((Long) row[0], (Long) row[1]);
                }
                qAnalytics.put("optionCounts", optionMap);
            } else if (question.getType() == QuestionType.LIKERT) {
                Double avgValue = answerRepository.averageLikertValue(question.getId());
                qAnalytics.put("averageValue", avgValue != null ? avgValue : 0);
            }

            questionAnalytics.add(qAnalytics);
        }
        summaryMap.put("questionAnalytics", questionAnalytics);

        String summaryJson;
        try {
            summaryJson = objectMapper.writeValueAsString(summaryMap);
        } catch (Exception e) {
            summaryJson = "{}";
        }

        ResultReport report = ResultReport.builder()
                .survey(survey)
                .title(title)
                .summaryData(summaryJson)
                .totalResponses((int) totalResponses)
                .completionRate(completionRate)
                .averageTimeSeconds(avgTimeSeconds)
                .generatedBy(generatedBy)
                .build();

        ResultReport saved = resultReportRepository.save(report);
        return mapToDTO(saved);
    }

    // Get all reports for a specific survey
    @Transactional(readOnly = true)
    public List<ResultReportDTO> getReportsBySurvey(Long surveyId) {
        return resultReportRepository.findBySurveyId(surveyId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get a single report by ID
    @Transactional(readOnly = true)
    public ResultReportDTO getReportById(Long id) {
        ResultReport report = resultReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Result Report", id));
        return mapToDTO(report);
    }

    // Delete a result report
    @Transactional
    public void deleteReport(Long id) {
        ResultReport report = resultReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Result Report", id));
        resultReportRepository.delete(report);
    }

    // Map ResultReport entity to DTO
    private ResultReportDTO mapToDTO(ResultReport report) {
        return ResultReportDTO.builder()
                .id(report.getId())
                .surveyId(report.getSurvey().getId())
                .title(report.getTitle())
                .summaryData(report.getSummaryData())
                .totalResponses(report.getTotalResponses())
                .completionRate(report.getCompletionRate())
                .averageTimeSeconds(report.getAverageTimeSeconds())
                .generatedByUsername(report.getGeneratedBy() != null ?
                        report.getGeneratedBy().getUsername() : null)
                .createdAt(report.getCreatedAt())
                .build();
    }
}
