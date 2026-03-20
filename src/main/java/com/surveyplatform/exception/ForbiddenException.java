package com.surveyplatform.exception;

// Custom exception thrown when a user attempts an action they are not authorized to perform
// Returns HTTP 403 status code when caught by the global exception handler
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
