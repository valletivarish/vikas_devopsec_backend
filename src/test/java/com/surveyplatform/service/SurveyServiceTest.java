package com.surveyplatform.service;

import com.surveyplatform.dto.QuestionDTO;
import com.surveyplatform.dto.SurveyDTO;
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

// Unit tests for SurveyService verifying survey CRUD business logic
@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {

    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SurveyResponseRepository surveyResponseRepository;

    @InjectMocks
    private SurveyService surveyService;

    // Test creating a survey with valid data succeeds
    @Test
    void createSurveyShouldReturnSurveyDTO() {
        User creator = User.builder().id(1L).username("testuser").build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(creator));

        Survey savedSurvey = Survey.builder()
                .id(1L)
                .title("Test Survey")
                .description("Test Description")
                .status(SurveyStatus.DRAFT)
                .visibility(SurveyVisibility.PUBLIC)
                .creator(creator)
                .shareLink("abc12345")
                .questions(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        when(surveyRepository.save(any(Survey.class))).thenReturn(savedSurvey);
        when(surveyResponseRepository.countBySurveyId(1L)).thenReturn(0L);

        QuestionDTO questionDTO = QuestionDTO.builder()
                .text("Sample question?")
                .type("MULTIPLE_CHOICE")
                .questionOrder(1)
                .build();

        SurveyDTO request = SurveyDTO.builder()
                .title("Test Survey")
                .description("Test Description")
                .questions(List.of(questionDTO))
                .build();

        SurveyDTO result = surveyService.createSurvey(request, "testuser");

        assertNotNull(result);
        assertEquals("Test Survey", result.getTitle());
        verify(surveyRepository).save(any(Survey.class));
    }

    // Test creating a survey with end date before start date throws exception
    @Test
    void createSurveyShouldThrowForInvalidDateRange() {
        User creator = User.builder().id(1L).username("testuser").build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(creator));

        SurveyDTO request = SurveyDTO.builder()
                .title("Test Survey")
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now())
                .questions(List.of(QuestionDTO.builder().text("Q?").type("OPEN_TEXT").questionOrder(1).build()))
                .build();

        assertThrows(BadRequestException.class,
                () -> surveyService.createSurvey(request, "testuser"));
    }

    // Test getting a survey by ID that does not exist throws ResourceNotFoundException
    @Test
    void getSurveyByIdShouldThrowWhenNotFound() {
        when(surveyRepository.findByIdWithQuestions(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> surveyService.getSurveyById(999L));
    }

    // Test deleting a survey by a different user throws BadRequestException
    @Test
    void deleteSurveyShouldThrowForNonOwner() {
        User creator = User.builder().id(1L).username("creator").build();
        Survey survey = Survey.builder().id(1L).creator(creator).build();
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(survey));

        assertThrows(BadRequestException.class,
                () -> surveyService.deleteSurvey(1L, "otheruser"));
    }

    // Test retrieving surveys for a user returns correct list
    @Test
    void getSurveysByUserShouldReturnList() {
        User user = User.builder().id(1L).username("testuser").build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Survey survey = Survey.builder()
                .id(1L).title("Survey 1").status(SurveyStatus.DRAFT)
                .visibility(SurveyVisibility.PUBLIC).creator(user)
                .questions(new ArrayList<>()).build();

        when(surveyRepository.findByCreator(user)).thenReturn(List.of(survey));
        when(surveyResponseRepository.countBySurveyId(1L)).thenReturn(5L);

        List<SurveyDTO> result = surveyService.getSurveysByUser("testuser");

        assertEquals(1, result.size());
        assertEquals("Survey 1", result.get(0).getTitle());
        assertEquals(5, result.get(0).getTotalResponses());
    }
}
