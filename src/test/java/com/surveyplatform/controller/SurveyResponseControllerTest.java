package com.surveyplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surveyplatform.config.CustomUserDetailsService;
import com.surveyplatform.config.JwtTokenProvider;
import com.surveyplatform.dto.SurveyResponseDTO;
import com.surveyplatform.service.SurveyResponseService;
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

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Unit tests for SurveyResponseController verifying response submission and retrieval
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SurveyResponseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private SurveyResponseService surveyResponseService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private String authToken;

    @BeforeEach
    void setUp() {
        authToken = jwtTokenProvider.generateToken("testuser");
        when(customUserDetailsService.loadUserByUsername("testuser"))
                .thenReturn(new User("testuser", "password",
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    // Test submitting a survey response returns 201
    @Test
    void submitResponseShouldReturnCreated() throws Exception {
        com.surveyplatform.dto.AnswerDTO answer = com.surveyplatform.dto.AnswerDTO.builder()
                .questionId(1L).answerValue("Yes").build();
        SurveyResponseDTO request = SurveyResponseDTO.builder()
                .surveyId(1L).completed(true).answers(List.of(answer)).build();
        SurveyResponseDTO response = SurveyResponseDTO.builder()
                .id(1L).surveyId(1L).completed(true).answers(List.of(answer)).build();

        when(surveyResponseService.submitResponse(any(SurveyResponseDTO.class), eq("testuser")))
                .thenReturn(response);

        mockMvc.perform(post("/api/responses")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    // Test getting responses for a survey returns list
    @Test
    void getResponsesBySurveyShouldReturnOk() throws Exception {
        SurveyResponseDTO dto = SurveyResponseDTO.builder()
                .id(1L).surveyId(1L).completed(true).build();

        when(surveyResponseService.getResponsesBySurvey(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/responses/survey/1")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // Test getting a response by ID
    @Test
    void getResponseByIdShouldReturnOk() throws Exception {
        SurveyResponseDTO dto = SurveyResponseDTO.builder()
                .id(1L).surveyId(1L).completed(true).build();

        when(surveyResponseService.getResponseById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/responses/1")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // Test accessing responses without auth returns 401
    @Test
    void getResponsesShouldReturnUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/responses/survey/1"))
                .andExpect(status().isUnauthorized());
    }
}
