package com.surveyplatform.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Unit tests for GlobalExceptionHandler verifying correct HTTP status and message mapping
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // Test ResourceNotFoundException returns 404
    @Test
    void handleResourceNotFoundShouldReturn404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Survey", 1L);

        ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("status"));
    }

    // Test BadRequestException returns 400
    @Test
    void handleBadRequestShouldReturn400() {
        BadRequestException ex = new BadRequestException("Invalid input");

        ResponseEntity<Map<String, Object>> response = handler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid input", response.getBody().get("message"));
    }

    // Test ForbiddenException returns 403
    @Test
    void handleForbiddenShouldReturn403() {
        ForbiddenException ex = new ForbiddenException("Access denied");

        ResponseEntity<Map<String, Object>> response = handler.handleForbidden(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access denied", response.getBody().get("message"));
    }

    // Test BadCredentialsException returns 401
    @Test
    void handleBadCredentialsShouldReturn401() {
        org.springframework.security.authentication.BadCredentialsException ex =
                new org.springframework.security.authentication.BadCredentialsException("Bad creds");

        ResponseEntity<Map<String, Object>> response = handler.handleBadCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid username or password", response.getBody().get("message"));
    }

    // Test IllegalArgumentException returns 400
    @Test
    void handleIllegalArgumentShouldReturn400() {
        IllegalArgumentException ex = new IllegalArgumentException("No enum constant");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").toString().contains("No enum constant"));
    }

    // Test generic Exception returns 500
    @Test
    void handleGenericExceptionShouldReturn500() {
        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
    }

    // Test error response contains timestamp
    @Test
    void errorResponseShouldContainTimestamp() {
        BadRequestException ex = new BadRequestException("Test");

        ResponseEntity<Map<String, Object>> response = handler.handleBadRequest(ex);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("timestamp"));
    }

    // Test error response contains error reason phrase
    @Test
    void errorResponseShouldContainErrorField() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Item", 1L);

        ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFound(ex);

        assertNotNull(response.getBody());
        assertEquals("Not Found", response.getBody().get("error"));
    }
}
