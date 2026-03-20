package com.surveyplatform.controller;

import com.surveyplatform.config.CustomUserDetailsService;
import com.surveyplatform.config.JwtTokenProvider;
import com.surveyplatform.dto.ResultReportDTO;
import com.surveyplatform.service.ResultReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Unit tests for ResultReportController verifying report generation and retrieval
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResultReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private ResultReportService resultReportService;

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

    // Test generating a report returns 201
    @Test
    void generateReportShouldReturnCreated() throws Exception {
        ResultReportDTO report = ResultReportDTO.builder()
                .id(1L).surveyId(1L).title("My Report")
                .totalResponses(10).completionRate(85.0)
                .createdAt(LocalDateTime.now()).build();

        when(resultReportService.generateReport(eq(1L), eq("My Report"), eq("testuser")))
                .thenReturn(report);

        mockMvc.perform(post("/api/reports/survey/1")
                        .header("Authorization", "Bearer " + authToken)
                        .param("title", "My Report"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("My Report"));
    }

    // Test getting reports for a survey
    @Test
    void getReportsBySurveyShouldReturnOk() throws Exception {
        ResultReportDTO report = ResultReportDTO.builder()
                .id(1L).surveyId(1L).title("Report").build();

        when(resultReportService.getReportsBySurvey(1L)).thenReturn(List.of(report));

        mockMvc.perform(get("/api/reports/survey/1")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Report"));
    }

    // Test getting a report by ID
    @Test
    void getReportByIdShouldReturnOk() throws Exception {
        ResultReportDTO report = ResultReportDTO.builder()
                .id(1L).surveyId(1L).title("Report").build();

        when(resultReportService.getReportById(1L)).thenReturn(report);

        mockMvc.perform(get("/api/reports/1")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // Test deleting a report returns 204
    @Test
    void deleteReportShouldReturnNoContent() throws Exception {
        doNothing().when(resultReportService).deleteReport(1L);

        mockMvc.perform(delete("/api/reports/1")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    // Test accessing reports without auth returns 401
    @Test
    void getReportsShouldReturnUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/reports/survey/1"))
                .andExpect(status().isUnauthorized());
    }
}
