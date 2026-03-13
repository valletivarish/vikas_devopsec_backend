package com.surveyplatform.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// JWT token provider responsible for generating, validating, and parsing JWT tokens
// Uses HMAC-SHA256 for token signing with a configurable secret key and expiration
@Component
public class JwtTokenProvider {

    // Secret key used for signing JWT tokens, loaded from application properties
    @Value("${jwt.secret}")
    private String jwtSecret;

    // Token expiration time in milliseconds (default 24 hours)
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // Generate a signing key from the configured secret string
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Generate a new JWT token for the authenticated user
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // Extract the username from a valid JWT token
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Validate the JWT token checking signature and expiration
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
