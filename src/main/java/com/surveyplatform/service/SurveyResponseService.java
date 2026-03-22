package com.surveyplatform.service;

import com.surveyplatform.dto.*;
import com.surveyplatform.exception.BadRequestException;
import com.surveyplatform.exception.ResourceNotFoundException;
import com.surveyplatform.model.*;
import com.surveyplatform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Service managing survey response submissions and retrieval
// Handles answer validation, completion tracking, and response analytics
@Service
public class SurveyResponseService {

    private final SurveyResponseRepository surveyResponseRepository;
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    // Constructor injection for all repository dependencies
    public SurveyResponseService(SurveyResponseRepository surveyResponseRepository,
                                  SurveyRepository surveyRepository,
                                  QuestionRepository questionRepository,
                                  UserRepository userRepository) {
        this.surveyResponseRepository = surveyResponseRepository;
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    // Submit a new survey response with answers to individual questions
    @Transactional
    public SurveyResponseDTO submitResponse(SurveyResponseDTO dto, String username) {
        Survey survey = surveyRepository.findById(dto.getSurveyId())
                .orElseThrow(() -> new ResourceNotFoundException("Survey", dto.getSurveyId()));

        // Validate that the survey is accepting responses
        if (survey.getStatus() != SurveyStatus.ACTIVE) {
            throw new BadRequestException("Survey is not currently accepting responses");
        }

        // Validate that the survey has not expired
        if (survey.getEndDate() != null && survey.getEndDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Survey has expired and is no longer accepting responses");
        }

        SurveyResponse response = SurveyResponse.builder()
                .survey(survey)
                .completed(dto.getCompleted() != null ? dto.getCompleted() : true)
                .submittedAt(LocalDateTime.now())
                .build();

        // Set respondent if authenticated
        if (username != null) {
            userRepository.findByUsername(username).ifPresent(response::setRespondent);
        }

        // Map answer DTOs to Answer entities
        if (dto.getAnswers() != null) {
            List<Answer> answers = new ArrayList<>();
            for (AnswerDTO answerDto : dto.getAnswers()) {
                Question question = questionRepository.findById(answerDto.getQuestionId())
                        .orElseThrow(() -> new ResourceNotFoundException("Question", answerDto.getQuestionId()));

                // Validate LIKERT answer values are numeric and within range
                if (question.getType() == QuestionType.LIKERT && answerDto.getAnswerValue() != null) {
                    try {
                        int value = Integer.parseInt(answerDto.getAnswerValue());
                        if (question.getLikertMin() != null && value < question.getLikertMin()) {
                            throw new BadRequestException("Answer value " + value + " is below the minimum " + question.getLikertMin() + " for question " + question.getId());
                        }
                        if (question.getLikertMax() != null && value > question.getLikertMax()) {
                            throw new BadRequestException("Answer value " + value + " exceeds the maximum " + question.getLikertMax() + " for question " + question.getId());
                        }
                    } catch (NumberFormatException e) {
                        throw new BadRequestException("LIKERT answer must be a numeric value for question " + question.getId());
                    }
                }

                Answer answer = Answer.builder()
                        .surveyResponse(response)
                        .question(question)
                        .answerValue(answerDto.getAnswerValue())
                        .selectedOptionId(answerDto.getSelectedOptionId())
                        .build();
                answers.add(answer);
            }
            response.setAnswers(answers);
        }

        SurveyResponse saved = surveyResponseRepository.save(response);
        return mapToDTO(saved);
    }

    // Get all responses for a specific survey (only the survey creator can view)
    @Transactional(readOnly = true)
    public List<SurveyResponseDTO> getResponsesBySurvey(Long surveyId, String username) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey", surveyId));
        if (!survey.getCreator().getUsername().equals(username)) {
            throw new BadRequestException("You can only view responses for your own surveys");
        }
        return surveyResponseRepository.findBySurveyId(surveyId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get a single response by its ID
    @Transactional(readOnly = true)
    public SurveyResponseDTO getResponseById(Long id) {
        SurveyResponse response = surveyResponseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey Response", id));
        return mapToDTO(response);
    }

    // Map SurveyResponse entity to DTO
    private SurveyResponseDTO mapToDTO(SurveyResponse response) {
        List<AnswerDTO> answerDTOs = response.getAnswers().stream()
                .map(answer -> AnswerDTO.builder()
                        .id(answer.getId())
                        .questionId(answer.getQuestion().getId())
                        .answerValue(answer.getAnswerValue())
                        .selectedOptionId(answer.getSelectedOptionId())
                        .build())
                .collect(Collectors.toList());

        return SurveyResponseDTO.builder()
                .id(response.getId())
                .surveyId(response.getSurvey().getId())
                .completed(response.getCompleted())
                .answers(answerDTOs)
                .respondentUsername(response.getRespondent() != null ?
                        response.getRespondent().getUsername() : "anonymous")
                .startedAt(response.getStartedAt())
                .submittedAt(response.getSubmittedAt())
                .build();
    }
}
