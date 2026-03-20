package com.surveyplatform.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

// Unit tests for JwtTokenProvider verifying token generation, parsing, and validation
@SpringBootTest
@ActiveProfiles("test")
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // Test generating a token returns a non-empty string
    @Test
    void generateTokenShouldReturnNonEmptyString() {
        String token = jwtTokenProvider.generateToken("testuser");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    // Test extracting username from a valid token
    @Test
    void getUsernameFromTokenShouldReturnCorrectUsername() {
        String token = jwtTokenProvider.generateToken("johndoe");

        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals("johndoe", username);
    }

    // Test validating a valid token returns true
    @Test
    void validateTokenShouldReturnTrueForValidToken() {
        String token = jwtTokenProvider.generateToken("testuser");

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    // Test validating an invalid token returns false
    @Test
    void validateTokenShouldReturnFalseForInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid.jwt.token"));
    }

    // Test validating a null token returns false
    @Test
    void validateTokenShouldReturnFalseForNullToken() {
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    // Test validating an empty token returns false
    @Test
    void validateTokenShouldReturnFalseForEmptyToken() {
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    // Test tokens generated for different users contain different usernames
    @Test
    void tokensShouldContainDifferentUsernamesForDifferentUsers() {
        String token1 = jwtTokenProvider.generateToken("user1");
        String token2 = jwtTokenProvider.generateToken("user2");

        assertEquals("user1", jwtTokenProvider.getUsernameFromToken(token1));
        assertEquals("user2", jwtTokenProvider.getUsernameFromToken(token2));
        assertNotEquals(token1, token2);
    }

    // Test validating a tampered token returns false
    @Test
    void validateTokenShouldReturnFalseForTamperedToken() {
        String token = jwtTokenProvider.generateToken("testuser");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertFalse(jwtTokenProvider.validateToken(tampered));
    }
}
