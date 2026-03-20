package com.surveyplatform.controller;

import com.surveyplatform.config.CustomUserDetailsService;
import com.surveyplatform.config.JwtTokenProvider;
import com.surveyplatform.dto.ForecastDTO;
import com.surveyplatform.service.ForecastService;
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

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Unit tests for ForecastController verifying forecast endpoint behavior
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ForecastControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private ForecastService forecastService;

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

    // Test getting forecast for a survey
    @Test
    void getForecastShouldReturnOk() throws Exception {
        ForecastDTO forecast = ForecastDTO.builder()
                .surveyId(1L).predictedCompletionRate(75.0)
                .confidenceScore(0.85).trendSlope(0.5).trendDirection("INCREASING")
                .historicalData(new ArrayList<>()).predictedData(new ArrayList<>()).build();

        when(forecastService.generateForecast(1L)).thenReturn(forecast);

        mockMvc.perform(get("/api/forecast/survey/1")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.surveyId").value(1))
                .andExpect(jsonPath("$.trendDirection").value("INCREASING"));
    }

    // Test getting all forecasts
    @Test
    void getAllForecastsShouldReturnOk() throws Exception {
        when(forecastService.generateAllForecasts("testuser")).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/forecast/all")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
    }

    // Test accessing forecast without auth returns 401
    @Test
    void getForecastShouldReturnUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/forecast/survey/1"))
                .andExpect(status().isUnauthorized());
    }
}
