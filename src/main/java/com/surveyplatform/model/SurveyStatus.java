package com.surveyplatform.model;

// Enum defining the lifecycle status of a survey
public enum SurveyStatus {
    DRAFT,      // Survey is being created and not yet published
    ACTIVE,     // Survey is published and accepting responses
    CLOSED      // Survey is closed and no longer accepting responses
}
