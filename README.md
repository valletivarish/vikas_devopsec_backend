# Serverless Polling and Survey Platform - Backend

## Overview

Spring Boot 3 REST API backend for the Serverless Polling and Survey Platform. Provides CRUD operations for surveys, questions, response options, survey responses, and result reports. Includes JWT authentication, input validation, ML-based response rate forecasting, and comprehensive API documentation.

## Tech Stack

- Java 17
- Spring Boot 3.2.5
- Spring Security 6 with JWT Authentication
- Spring Data JPA with PostgreSQL
- Apache Commons Math 3 (SimpleRegression for ML forecasting)
- Springdoc OpenAPI (Swagger UI)
- Lombok
- Maven

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 14+

## Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE surveyplatform;
```

Update credentials in `src/main/resources/application.properties` if different from defaults:
- Username: default
- Password: root

## Running Locally

```bash
mvn clean install
mvn spring-boot:run
```

The application starts on port 8080.

## API Documentation

Swagger UI is available at: http://localhost:8080/swagger-ui.html

## API Endpoints

### Authentication
- POST /api/auth/register - Register a new user
- POST /api/auth/login - Login and receive JWT token

### Surveys (CRUD)
- POST /api/surveys - Create a new survey
- GET /api/surveys - Get surveys for authenticated user
- GET /api/surveys/{id} - Get survey by ID
- GET /api/surveys/share/{shareLink} - Get survey by share link (public)
- PUT /api/surveys/{id} - Update a survey
- DELETE /api/surveys/{id} - Delete a survey

### Questions (CRUD)
- POST /api/questions/survey/{surveyId} - Add question to survey
- GET /api/questions/survey/{surveyId} - Get questions for survey
- GET /api/questions/{id} - Get question by ID
- PUT /api/questions/{id} - Update a question
- DELETE /api/questions/{id} - Delete a question

### Survey Responses
- POST /api/responses - Submit a survey response
- GET /api/responses/survey/{surveyId} - Get responses for survey
- GET /api/responses/{id} - Get response by ID

### Result Reports
- POST /api/reports/survey/{surveyId} - Generate report
- GET /api/reports/survey/{surveyId} - Get reports for survey
- GET /api/reports/{id} - Get report by ID
- DELETE /api/reports/{id} - Delete a report

### Analytics
- GET /api/dashboard - Get dashboard analytics
- GET /api/forecast/survey/{surveyId} - Get response rate forecast
- GET /api/forecast/all - Get all forecasts

### Health
- GET /api/health - Health check endpoint

## Static Analysis

Run all analysis tools:

```bash
mvn verify
```

This runs:
- SpotBugs (bug detection)
- PMD (code style and complexity)
- JaCoCo (code coverage, minimum 60%)

## Testing

```bash
mvn test
```

Tests use H2 in-memory database configured in `src/test/resources/application-test.properties`.

## Project Structure

```
src/main/java/com/surveyplatform/
    config/         Security, JWT, CORS configuration
    controller/     REST API controllers
    dto/            Data Transfer Objects with validation
    model/          JPA entities and enums
    repository/     Spring Data JPA repositories
    service/        Business logic layer
    exception/      Global exception handler
```

## CI/CD

GitHub Actions pipeline in `.github/workflows/ci-cd.yml` handles:
- CI: Build, test, static analysis, security scanning
- CD: Deploy to AWS EC2 via SSH

## Infrastructure

Terraform configuration in `terraform/` provisions:
- AWS VPC with subnets
- EC2 instance for the backend
- RDS PostgreSQL database
- S3 bucket for artifacts
