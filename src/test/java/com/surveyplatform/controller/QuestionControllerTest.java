package com.surveyplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surveyplatform.config.CustomUserDetailsService;
import com.surveyplatform.config.JwtTokenProvider;
import com.surveyplatform.dto.QuestionDTO;
import com.surveyplatform.model.*;
import com.surveyplatform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Unit tests for QuestionController verifying question CRUD endpoints
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private QuestionRepository questionRepository;

    @MockBean
    private SurveyRepository surveyRepository;

    @MockBean
    private ResponseOptionRepository responseOptionRepository;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private SurveyResponseRepository surveyResponseRepository;

    @MockBean
    private AnswerRepository answerRepository;

    @MockBean
    private ResultReportRepository resultReportRepository;

    @MockBean
    private UserRepository userRepository;

    private String authToken;

    @BeforeEach
    void setUp() {
        authToken = jwtTokenProvider.generateToken("testuser");
        when(customUserDetailsService.loadUserByUsername("testuser"))
                .thenReturn(new User("testuser", "password",
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    // Test getting questions for a survey
    @Test
    void getQuestionsBySurveyShouldReturnOk() throws Exception {
        Question question = Question.builder()
                .id(1L).text("How are you?").type(QuestionType.OPEN_TEXT)
                .questionOrder(1).required(true).responseOptions(new ArrayList<>()).build();

        when(questionRepository.findBySurveyIdOrderByQuestionOrderAsc(1L))
                .thenReturn(List.of(question));

        mockMvc.perform(get("/api/questions/survey/1")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("How are you?"));
    }

    // Test getting a question by ID
    @Test
    void getQuestionByIdShouldReturnOk() throws Exception {
        Question question = Question.builder()
                .id(1L).text("Favorite color?").type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(1).required(true).responseOptions(new ArrayList<>()).build();

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        mockMvc.perform(get("/api/questions/1")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Favorite color?"));
    }

    // Test creating a question for a survey
    @Test
    void createQuestionShouldReturnCreated() throws Exception {
        Survey survey = Survey.builder().id(1L).title("Test Survey").build();
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(survey));

        Question saved = Question.builder()
                .id(1L).text("Rate our service").type(QuestionType.LIKERT)
                .questionOrder(1).required(true).likertMin(1).likertMax(5)
                .survey(survey).responseOptions(new ArrayList<>()).build();
        when(questionRepository.save(any(Question.class))).thenReturn(saved);

        QuestionDTO dto = QuestionDTO.builder()
                .text("Rate our service").type("LIKERT")
                .questionOrder(1).required(true).likertMin(1).likertMax(5).build();

        mockMvc.perform(post("/api/questions/survey/1")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("Rate our service"));
    }

    // Test deleting a question
    @Test
    void deleteQuestionShouldReturnNoContent() throws Exception {
        Question question = Question.builder().id(1L).text("Q").type(QuestionType.OPEN_TEXT)
                .questionOrder(1).required(true).responseOptions(new ArrayList<>()).build();
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        mockMvc.perform(delete("/api/questions/1")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    // Test accessing questions without auth returns 401
    @Test
    void getQuestionsShouldReturnUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/questions/survey/1"))
                .andExpect(status().isUnauthorized());
    }
}
