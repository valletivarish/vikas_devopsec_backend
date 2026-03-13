package com.surveyplatform.exception;

// Custom exception thrown when a requested resource is not found in the database
// Returns HTTP 404 status code when caught by the global exception handler
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id);
    }
}
