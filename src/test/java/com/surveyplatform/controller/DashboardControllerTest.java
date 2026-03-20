package com.surveyplatform.controller;

import com.surveyplatform.config.CustomUserDetailsService;
import com.surveyplatform.config.JwtTokenProvider;
import com.surveyplatform.dto.DashboardDTO;
import com.surveyplatform.service.DashboardService;
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

// Unit tests for DashboardController verifying dashboard endpoint behavior
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private DashboardService dashboardService;

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

    // Test getting dashboard data for authenticated user
    @Test
    void getDashboardShouldReturnOk() throws Exception {
        DashboardDTO dashboard = DashboardDTO.builder()
                .totalSurveys(5L).activeSurveys(3L).totalResponses(20L)
                .averageCompletionRate(85.0)
                .responsesPerSurvey(new ArrayList<>())
                .recentActivity(new ArrayList<>())
                .build();

        when(dashboardService.getDashboard("testuser")).thenReturn(dashboard);

        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSurveys").value(5))
                .andExpect(jsonPath("$.activeSurveys").value(3));
    }

    // Test accessing dashboard without auth returns 401
    @Test
    void getDashboardShouldReturnUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isUnauthorized());
    }
}
