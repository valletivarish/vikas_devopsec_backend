package com.surveyplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surveyplatform.config.JwtTokenProvider;
import com.surveyplatform.dto.QuestionDTO;
import com.surveyplatform.dto.ResponseOptionDTO;
import com.surveyplatform.dto.SurveyDTO;
import com.surveyplatform.config.CustomUserDetailsService;
import com.surveyplatform.service.SurveyService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Unit tests for SurveyController verifying CRUD operations and input validation
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SurveyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private SurveyService surveyService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private String authToken;

    @BeforeEach
    void setUp() {
        // Generate a valid JWT token for authenticated requests
        authToken = jwtTokenProvider.generateToken("testuser");

        // Mock the user details service so the JWT auth filter can resolve "testuser"
        when(customUserDetailsService.loadUserByUsername("testuser"))
                .thenReturn(new User("testuser", "password",
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    // Test creating a survey with valid input returns HTTP 201
    @Test
    void createSurveyShouldReturnCreated() throws Exception {
        SurveyDTO request = buildSampleSurveyDTO();
        SurveyDTO response = buildSampleSurveyDTO();
        response.setId(1L);
        response.setShareLink("abc12345");

        when(surveyService.createSurvey(any(SurveyDTO.class), eq("testuser"))).thenReturn(response);

        mockMvc.perform(post("/api/surveys")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Customer Satisfaction Survey"));
    }

    // Test creating a survey without a title returns validation error
    @Test
    void createSurveyShouldReturnBadRequestForMissingTitle() throws Exception {
        SurveyDTO request = new SurveyDTO();
        request.setDescription("Test description");

        mockMvc.perform(post("/api/surveys")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Test getting all surveys for authenticated user
    @Test
    void getMySurveysShouldReturnList() throws Exception {
        SurveyDTO survey = buildSampleSurveyDTO();
        survey.setId(1L);
        when(surveyService.getSurveysByUser("testuser")).thenReturn(List.of(survey));

        mockMvc.perform(get("/api/surveys")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Customer Satisfaction Survey"));
    }

    // Test getting a survey by ID
    @Test
    void getSurveyByIdShouldReturnSurvey() throws Exception {
        SurveyDTO survey = buildSampleSurveyDTO();
        survey.setId(1L);
        when(surveyService.getSurveyById(1L)).thenReturn(survey);

        mockMvc.perform(get("/api/surveys/1")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Customer Satisfaction Survey"));
    }

    // Test deleting a survey returns HTTP 204
    @Test
    void deleteSurveyShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/surveys/1")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    // Test accessing surveys without authentication returns HTTP 401
    @Test
    void getSurveysShouldReturnUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/surveys"))
                .andExpect(status().isUnauthorized());
    }

    // Helper method to build a sample survey DTO for testing
    private SurveyDTO buildSampleSurveyDTO() {
        ResponseOptionDTO option1 = ResponseOptionDTO.builder()
                .text("Very Satisfied").optionOrder(1).build();
        ResponseOptionDTO option2 = ResponseOptionDTO.builder()
                .text("Satisfied").optionOrder(2).build();

        QuestionDTO question = QuestionDTO.builder()
                .text("How satisfied are you with our service?")
                .type("MULTIPLE_CHOICE")
                .questionOrder(1)
                .required(true)
                .responseOptions(Arrays.asList(option1, option2))
                .build();

        return SurveyDTO.builder()
                .title("Customer Satisfaction Survey")
                .description("Help us improve our services")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(30))
                .visibility("PUBLIC")
                .questions(List.of(question))
                .build();
    }
}
