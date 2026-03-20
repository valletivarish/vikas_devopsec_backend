package com.surveyplatform.service;

import com.surveyplatform.config.JwtTokenProvider;
import com.surveyplatform.dto.AuthRequest;
import com.surveyplatform.dto.AuthResponse;
import com.surveyplatform.dto.RegisterRequest;
import com.surveyplatform.exception.BadRequestException;
import com.surveyplatform.model.Role;
import com.surveyplatform.model.User;
import com.surveyplatform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

// Unit tests for AuthService verifying registration and login business logic
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    // Test successful registration creates user and returns token
    @Test
    void registerShouldReturnAuthResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setFullName("New User");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.generateToken("newuser")).thenReturn("test-jwt-token");

        var response = authService.register(request);

        assertNotNull(response);
        assertEquals("newuser", response.getUsername());
        assertEquals("test-jwt-token", response.getToken());
        assertEquals("USER", response.getRole());
    }

    // Test registration fails when username already exists
    @Test
    void registerShouldThrowExceptionForDuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("new@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
    }

    // Test registration fails when email already exists
    @Test
    void registerShouldThrowExceptionForDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
    }

    // Test successful login returns auth response with token
    @Test
    void loginShouldReturnAuthResponse() {
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        User user = User.builder()
                .id(1L).username("testuser").email("test@example.com")
                .password("encoded").fullName("Test User").role(Role.USER).build();

        when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.of(user));
        when(jwtTokenProvider.generateToken("testuser")).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("jwt-token", response.getToken());
        assertEquals("USER", response.getRole());
    }

    // Test login with non-existent user throws exception
    @Test
    void loginShouldThrowForNonExistentUser() {
        AuthRequest request = new AuthRequest();
        request.setUsername("unknown");
        request.setPassword("password");

        when(userRepository.findByUsername("unknown")).thenReturn(java.util.Optional.empty());

        assertThrows(BadRequestException.class, () -> authService.login(request));
    }
}
