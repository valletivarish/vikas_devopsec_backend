package com.surveyplatform.service;

import com.surveyplatform.dto.AnswerDTO;
import com.surveyplatform.dto.SurveyResponseDTO;
import com.surveyplatform.exception.BadRequestException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Unit tests for SurveyResponseService verifying response submission and retrieval
@ExtendWith(MockitoExtension.class)
class SurveyResponseServiceTest {

    @Mock
    private SurveyResponseRepository surveyResponseRepository;

    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SurveyResponseService surveyResponseService;

    // Test submitting a valid response to an active survey
    @Test
    void submitResponseShouldSucceedForActiveSurvey() {
        Survey survey = Survey.builder().id(1L).status(SurveyStatus.ACTIVE)
                .endDate(LocalDateTime.now().plusDays(7)).build();
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(survey));

        SurveyResponse savedResponse = SurveyResponse.builder()
                .id(1L).survey(survey).completed(true)
                .submittedAt(LocalDateTime.now()).answers(new ArrayList<>()).build();
        when(surveyResponseRepository.save(any(SurveyResponse.class))).thenReturn(savedResponse);

        SurveyResponseDTO dto = SurveyResponseDTO.builder()
                .surveyId(1L).completed(true).build();

        SurveyResponseDTO result = surveyResponseService.submitResponse(dto, null);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(surveyResponseRepository).save(any(SurveyResponse.class));
    }

    // Test submitting response to inactive survey throws exception
    @Test
    void submitResponseShouldThrowForInactiveSurvey() {
        Survey survey = Survey.builder().id(1L).status(SurveyStatus.DRAFT).build();
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(survey));

        SurveyResponseDTO dto = SurveyResponseDTO.builder().surveyId(1L).build();

        assertThrows(BadRequestException.class,
                () -> surveyResponseService.submitResponse(dto, null));
    }

    // Test submitting response to expired survey throws exception
    @Test
    void submitResponseShouldThrowForExpiredSurvey() {
        Survey survey = Survey.builder().id(1L).status(SurveyStatus.ACTIVE)
                .endDate(LocalDateTime.now().minusDays(1)).build();
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(survey));

        SurveyResponseDTO dto = SurveyResponseDTO.builder().surveyId(1L).build();

        assertThrows(BadRequestException.class,
                () -> surveyResponseService.submitResponse(dto, null));
    }

    // Test submitting response to nonexistent survey throws exception
    @Test
    void submitResponseShouldThrowForMissingSurvey() {
        when(surveyRepository.findById(999L)).thenReturn(Optional.empty());

        SurveyResponseDTO dto = SurveyResponseDTO.builder().surveyId(999L).build();

        assertThrows(ResourceNotFoundException.class,
                () -> surveyResponseService.submitResponse(dto, null));
    }

    // Test submitting response with LIKERT answer validation
    @Test
    void submitResponseShouldValidateLikertRange() {
        Survey survey = Survey.builder().id(1L).status(SurveyStatus.ACTIVE).build();
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(survey));

        Question question = Question.builder().id(10L).type(QuestionType.LIKERT)
                .likertMin(1).likertMax(5).build();
        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        AnswerDTO answerDto = AnswerDTO.builder().questionId(10L).answerValue("10").build();
        SurveyResponseDTO dto = SurveyResponseDTO.builder()
                .surveyId(1L).completed(true).answers(List.of(answerDto)).build();

        assertThrows(BadRequestException.class,
                () -> surveyResponseService.submitResponse(dto, null));
    }

    // Test submitting response with non-numeric LIKERT answer throws exception
    @Test
    void submitResponseShouldThrowForNonNumericLikert() {
        Survey survey = Survey.builder().id(1L).status(SurveyStatus.ACTIVE).build();
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(survey));

        Question question = Question.builder().id(10L).type(QuestionType.LIKERT)
                .likertMin(1).likertMax(5).build();
        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        AnswerDTO answerDto = AnswerDTO.builder().questionId(10L).answerValue("abc").build();
        SurveyResponseDTO dto = SurveyResponseDTO.builder()
                .surveyId(1L).completed(true).answers(List.of(answerDto)).build();

        assertThrows(BadRequestException.class,
                () -> surveyResponseService.submitResponse(dto, null));
    }

    // Test submitting response with LIKERT below minimum throws exception
    @Test
    void submitResponseShouldThrowForLikertBelowMin() {
        Survey survey = Survey.builder().id(1L).status(SurveyStatus.ACTIVE).build();
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(survey));

        Question question = Question.builder().id(10L).type(QuestionType.LIKERT)
                .likertMin(1).likertMax(5).build();
        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        AnswerDTO answerDto = AnswerDTO.builder().questionId(10L).answerValue("0").build();
        SurveyResponseDTO dto = SurveyResponseDTO.builder()
                .surveyId(1L).completed(true).answers(List.of(answerDto)).build();

        assertThrows(BadRequestException.class,
                () -> surveyResponseService.submitResponse(dto, null));
    }

    // Test getting responses by survey ID (as the survey creator)
    @Test
    void getResponsesBySurveyShouldReturnList() {
        User creator = User.builder().id(1L).username("creator").build();
        Survey survey = Survey.builder().id(1L).creator(creator).build();
        Question question = Question.builder().id(10L).build();
        Answer answer = Answer.builder().id(1L).question(question).answerValue("Yes").build();
        SurveyResponse response = SurveyResponse.builder()
                .id(1L).survey(survey).completed(true)
                .answers(List.of(answer))
                .submittedAt(LocalDateTime.now()).build();

        when(surveyRepository.findById(1L)).thenReturn(Optional.of(survey));
        when(surveyResponseRepository.findBySurveyId(1L)).thenReturn(List.of(response));

        List<SurveyResponseDTO> result = surveyResponseService.getResponsesBySurvey(1L, "creator");

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    // Test getting a response by ID that does not exist throws exception
    @Test
    void getResponseByIdShouldThrowWhenNotFound() {
        when(surveyResponseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> surveyResponseService.getResponseById(999L));
    }

    // Test getting a response by valid ID
    @Test
    void getResponseByIdShouldReturnResponse() {
        Survey survey = Survey.builder().id(1L).build();
        SurveyResponse response = SurveyResponse.builder()
                .id(1L).survey(survey).completed(true)
                .answers(new ArrayList<>())
                .submittedAt(LocalDateTime.now()).build();

        when(surveyResponseRepository.findById(1L)).thenReturn(Optional.of(response));

        SurveyResponseDTO result = surveyResponseService.getResponseById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    // Test submitting response with authenticated user sets respondent
    @Test
    void submitResponseShouldSetRespondentForAuthenticatedUser() {
        Survey survey = Survey.builder().id(1L).status(SurveyStatus.ACTIVE).build();
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(survey));

        User user = User.builder().id(1L).username("testuser").build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        SurveyResponse savedResponse = SurveyResponse.builder()
                .id(1L).survey(survey).respondent(user).completed(true)
                .submittedAt(LocalDateTime.now()).answers(new ArrayList<>()).build();
        when(surveyResponseRepository.save(any(SurveyResponse.class))).thenReturn(savedResponse);

        SurveyResponseDTO dto = SurveyResponseDTO.builder()
                .surveyId(1L).completed(true).build();

        SurveyResponseDTO result = surveyResponseService.submitResponse(dto, "testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getRespondentUsername());
    }
}
