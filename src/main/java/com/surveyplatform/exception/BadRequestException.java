package com.surveyplatform.exception;

// Custom exception thrown when a client request contains invalid data or violates business rules
// Returns HTTP 400 status code when caught by the global exception handler
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
