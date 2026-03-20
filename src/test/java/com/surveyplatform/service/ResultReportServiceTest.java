package com.surveyplatform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surveyplatform.dto.ResultReportDTO;
import com.surveyplatform.exception.ResourceNotFoundException;
import com.surveyplatform.model.*;
import com.surveyplatform.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Unit tests for ResultReportService verifying report generation and retrieval
@ExtendWith(MockitoExtension.class)
class ResultReportServiceTest {

    @Mock
    private ResultReportRepository resultReportRepository;

    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private SurveyResponseRepository surveyResponseRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ResultReportService resultReportService;

    // Test generating a report for a survey with no responses
    @Test
    void generateReportShouldSucceedWithNoResponses() {
        Survey survey = Survey.builder().id(1L).title("Test Survey")
                .questions(new ArrayList<>()).build();
        User user = User.builder().id(1L).username("testuser").build();

        when(surveyRepository.findById(1L)).thenReturn(Optional.of(survey));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(surveyResponseRepository.findBySurveyId(1L)).thenReturn(new ArrayList<>());

        ResultReport savedReport = ResultReport.builder()
                .id(1L).survey(survey).title("Report 1").totalResponses(0)
                .completionRate(0.0).averageTimeSeconds(0L).generatedBy(user)
                .summaryData("{}").createdAt(LocalDateTime.now()).build();
        when(resultReportRepository.save(any(ResultReport.class))).thenReturn(savedReport);

        ResultReportDTO result = resultReportService.generateReport(1L, "Report 1", "testuser");

        assertNotNull(result);
        assertEquals("Report 1", result.getTitle());
        assertEquals(0, result.getTotalResponses());
    }

    // Test generating report for nonexistent survey throws exception
    @Test
    void generateReportShouldThrowForMissingSurvey() {
        when(surveyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> resultReportService.generateReport(999L, "Report", "testuser"));
    }

    // Test generating report for nonexistent user throws exception
    @Test
    void generateReportShouldThrowForMissingUser() {
        Survey survey = Survey.builder().id(1L).build();
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(survey));
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> resultReportService.generateReport(1L, "Report", "unknown"));
    }

    // Test getting reports by survey returns list
    @Test
    void getReportsBySurveyShouldReturnList() {
        Survey survey = Survey.builder().id(1L).build();
        User user = User.builder().id(1L).username("testuser").build();
        ResultReport report = ResultReport.builder()
                .id(1L).survey(survey).title("Report").totalResponses(5)
                .completionRate(80.0).averageTimeSeconds(120L).generatedBy(user)
                .summaryData("{}").createdAt(LocalDateTime.now()).build();

        when(resultReportRepository.findBySurveyId(1L)).thenReturn(List.of(report));

        List<ResultReportDTO> result = resultReportService.getReportsBySurvey(1L);

        assertEquals(1, result.size());
        assertEquals("Report", result.get(0).getTitle());
    }

    // Test getting a report by ID that does not exist throws exception
    @Test
    void getReportByIdShouldThrowWhenNotFound() {
        when(resultReportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> resultReportService.getReportById(999L));
    }

    // Test getting a report by valid ID
    @Test
    void getReportByIdShouldReturnReport() {
        Survey survey = Survey.builder().id(1L).build();
        User user = User.builder().id(1L).username("testuser").build();
        ResultReport report = ResultReport.builder()
                .id(1L).survey(survey).title("Report").totalResponses(5)
                .completionRate(80.0).averageTimeSeconds(120L).generatedBy(user)
                .summaryData("{}").createdAt(LocalDateTime.now()).build();

        when(resultReportRepository.findById(1L)).thenReturn(Optional.of(report));

        ResultReportDTO result = resultReportService.getReportById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    // Test deleting a report
    @Test
    void deleteReportShouldSucceed() {
        ResultReport report = ResultReport.builder().id(1L).build();
        when(resultReportRepository.findById(1L)).thenReturn(Optional.of(report));

        resultReportService.deleteReport(1L);

        verify(resultReportRepository).delete(report);
    }

    // Test deleting nonexistent report throws exception
    @Test
    void deleteReportShouldThrowWhenNotFound() {
        when(resultReportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> resultReportService.deleteReport(999L));
    }

    // Test generating report with responses calculates completion rate
    @Test
    void generateReportShouldCalculateCompletionRate() {
        Survey survey = Survey.builder().id(1L).title("Test Survey")
                .questions(new ArrayList<>()).build();
        User user = User.builder().id(1L).username("testuser").build();

        SurveyResponse r1 = SurveyResponse.builder().id(1L).completed(true).build();
        SurveyResponse r2 = SurveyResponse.builder().id(2L).completed(false).build();

        when(surveyRepository.findById(1L)).thenReturn(Optional.of(survey));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(surveyResponseRepository.findBySurveyId(1L)).thenReturn(List.of(r1, r2));

        ResultReport savedReport = ResultReport.builder()
                .id(1L).survey(survey).title("Report").totalResponses(2)
                .completionRate(50.0).averageTimeSeconds(0L).generatedBy(user)
                .summaryData("{}").createdAt(LocalDateTime.now()).build();
        when(resultReportRepository.save(any(ResultReport.class))).thenReturn(savedReport);

        ResultReportDTO result = resultReportService.generateReport(1L, "Report", "testuser");

        assertEquals(2, result.getTotalResponses());
    }
}
