package com.surveyplatform.service;

import com.surveyplatform.dto.DashboardDTO;
import com.surveyplatform.exception.ResourceNotFoundException;
import com.surveyplatform.model.*;
import com.surveyplatform.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Unit tests for DashboardService verifying dashboard analytics generation
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private SurveyResponseRepository surveyResponseRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardService dashboardService;

    // Test getting dashboard data for a user with surveys
    @Test
    void getDashboardShouldReturnAnalytics() {
        User user = User.builder().id(1L).username("testuser").build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Survey survey = Survey.builder().id(1L).title("Survey 1")
                .status(SurveyStatus.ACTIVE).creator(user)
                .createdAt(LocalDateTime.now()).build();

        when(surveyRepository.countByCreatorId(1L)).thenReturn(1L);
        when(surveyRepository.countByCreatorIdAndStatus(1L, SurveyStatus.ACTIVE)).thenReturn(1L);
        when(surveyResponseRepository.countByCreatorId(1L)).thenReturn(5L);
        when(surveyRepository.findByCreatorId(1L)).thenReturn(List.of(survey));
        when(surveyResponseRepository.countBySurveyId(1L)).thenReturn(5L);
        when(surveyResponseRepository.countBySurveyIdAndCompleted(1L, true)).thenReturn(4L);
        when(surveyResponseRepository.countResponsesPerSurvey(1L)).thenReturn(new ArrayList<>());
        for (QuestionType type : QuestionType.values()) {
            when(questionRepository.countByType(type)).thenReturn(0L);
        }

        DashboardDTO result = dashboardService.getDashboard("testuser");

        assertNotNull(result);
        assertEquals(1, result.getTotalSurveys());
        assertEquals(5, result.getTotalResponses());
        assertEquals(1, result.getActiveSurveys());
    }

    // Test getting dashboard for nonexistent user throws exception
    @Test
    void getDashboardShouldThrowForMissingUser() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> dashboardService.getDashboard("unknown"));
    }

    // Test dashboard with no surveys returns zeros
    @Test
    void getDashboardShouldReturnZerosForNewUser() {
        User user = User.builder().id(1L).username("newuser").build();
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(user));
        when(surveyRepository.countByCreatorId(1L)).thenReturn(0L);
        when(surveyRepository.countByCreatorIdAndStatus(1L, SurveyStatus.ACTIVE)).thenReturn(0L);
        when(surveyResponseRepository.countByCreatorId(1L)).thenReturn(0L);
        when(surveyRepository.findByCreatorId(1L)).thenReturn(new ArrayList<>());
        when(surveyResponseRepository.countResponsesPerSurvey(1L)).thenReturn(new ArrayList<>());
        for (QuestionType type : QuestionType.values()) {
            when(questionRepository.countByType(type)).thenReturn(0L);
        }

        DashboardDTO result = dashboardService.getDashboard("newuser");

        assertEquals(0, result.getTotalSurveys());
        assertEquals(0, result.getTotalResponses());
        assertEquals(0, result.getActiveSurveys());
        assertEquals(0.0, result.getAverageCompletionRate());
    }
}
