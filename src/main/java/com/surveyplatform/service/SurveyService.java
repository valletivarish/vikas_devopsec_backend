package com.surveyplatform.service;

import com.surveyplatform.dto.*;
import com.surveyplatform.exception.BadRequestException;
import com.surveyplatform.exception.ForbiddenException;
import com.surveyplatform.exception.ResourceNotFoundException;
import com.surveyplatform.model.*;
import com.surveyplatform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

// Service managing all survey CRUD operations and business logic
// Handles survey creation with nested questions and response options
@Service
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final SurveyResponseRepository surveyResponseRepository;

    // Constructor injection for all repository dependencies
    public SurveyService(SurveyRepository surveyRepository,
                         UserRepository userRepository,
                         SurveyResponseRepository surveyResponseRepository) {
        this.surveyRepository = surveyRepository;
        this.userRepository = userRepository;
        this.surveyResponseRepository = surveyResponseRepository;
    }

    // Create a new survey with nested questions and response options
    @Transactional
    public SurveyDTO createSurvey(SurveyDTO dto, String username) {
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Validate that end date is after start date when both are provided
        if (dto.getStartDate() != null && dto.getEndDate() != null
                && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        Survey survey = Survey.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .visibility(dto.getVisibility() != null ?
                        SurveyVisibility.valueOf(dto.getVisibility()) : SurveyVisibility.PUBLIC)
                .status(SurveyStatus.DRAFT)
                .creator(creator)
                .shareLink(UUID.randomUUID().toString().substring(0, 8))
                .build();

        // Build nested questions and response options from DTO
        if (dto.getQuestions() != null) {
            for (QuestionDTO qDto : dto.getQuestions()) {
                Question question = mapToQuestion(qDto, survey);
                survey.getQuestions().add(question);
            }
        }

        Survey saved = surveyRepository.save(survey);
        return mapToDTO(saved);
    }

    // Retrieve all surveys created by a specific user
    @Transactional(readOnly = true)
    public List<SurveyDTO> getSurveysByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return surveyRepository.findByCreator(user).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Retrieve all surveys (admin functionality)
    @Transactional(readOnly = true)
    public List<SurveyDTO> getAllSurveys() {
        return surveyRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Retrieve a single survey by ID with all nested data
    @Transactional(readOnly = true)
    public SurveyDTO getSurveyById(Long id) {
        Survey survey = surveyRepository.findByIdWithQuestions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey", id));
        return mapToDTO(survey);
    }

    // Retrieve a survey by its unique share link for public access
    @Transactional(readOnly = true)
    public SurveyDTO getSurveyByShareLink(String shareLink) {
        Survey survey = surveyRepository.findByShareLink(shareLink)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with share link: " + shareLink));
        return mapToDTO(survey);
    }

    // Update an existing survey with new data, replacing questions entirely
    @Transactional
    public SurveyDTO updateSurvey(Long id, SurveyDTO dto, String username) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey", id));

        // Verify the requesting user is the survey creator
        if (!survey.getCreator().getUsername().equals(username)) {
            throw new ForbiddenException("You can only update your own surveys");
        }

        // Validate date range
        if (dto.getStartDate() != null && dto.getEndDate() != null
                && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        survey.setTitle(dto.getTitle());
        survey.setDescription(dto.getDescription());
        survey.setStartDate(dto.getStartDate());
        survey.setEndDate(dto.getEndDate());

        if (dto.getVisibility() != null) {
            survey.setVisibility(SurveyVisibility.valueOf(dto.getVisibility()));
        }
        if (dto.getStatus() != null) {
            survey.setStatus(SurveyStatus.valueOf(dto.getStatus()));
        }

        // Update questions: match existing by ID, update in-place, add new, remove deleted
        if (dto.getQuestions() != null) {
            Map<Long, Question> existingQuestions = survey.getQuestions().stream()
                    .filter(q -> q.getId() != null)
                    .collect(Collectors.toMap(Question::getId, q -> q));

            Set<Long> incomingIds = dto.getQuestions().stream()
                    .map(QuestionDTO::getId)
                    .filter(qId -> qId != null)
                    .collect(Collectors.toSet());

            // Remove questions that are no longer in the DTO (only if they have no answers)
            survey.getQuestions().removeIf(q -> q.getId() != null && !incomingIds.contains(q.getId()));

            // Clear and rebuild the list in order
            List<Question> updatedQuestions = new ArrayList<>();
            for (QuestionDTO qDto : dto.getQuestions()) {
                if (qDto.getId() != null && existingQuestions.containsKey(qDto.getId())) {
                    // Update existing question in-place
                    Question existing = existingQuestions.get(qDto.getId());
                    existing.setText(qDto.getText());
                    existing.setType(QuestionType.valueOf(qDto.getType()));
                    existing.setQuestionOrder(qDto.getQuestionOrder());
                    existing.setRequired(qDto.getRequired() != null ? qDto.getRequired() : true);
                    existing.setLikertMin(qDto.getLikertMin());
                    existing.setLikertMax(qDto.getLikertMax());
                    existing.setMaxTextLength(qDto.getMaxTextLength());
                    existing.setConditionalQuestionId(qDto.getConditionalQuestionId());
                    existing.setConditionalAnswer(qDto.getConditionalAnswer());
                    // Update response options
                    existing.getResponseOptions().clear();
                    if (qDto.getResponseOptions() != null) {
                        for (ResponseOptionDTO optDto : qDto.getResponseOptions()) {
                            ResponseOption option = ResponseOption.builder()
                                    .text(optDto.getText())
                                    .optionOrder(optDto.getOptionOrder())
                                    .question(existing)
                                    .build();
                            existing.getResponseOptions().add(option);
                        }
                    }
                    updatedQuestions.add(existing);
                } else {
                    // New question
                    Question newQuestion = mapToQuestion(qDto, survey);
                    updatedQuestions.add(newQuestion);
                }
            }
            survey.getQuestions().clear();
            survey.getQuestions().addAll(updatedQuestions);
        }

        Survey saved = surveyRepository.save(survey);
        return mapToDTO(saved);
    }

    // Update survey status (DRAFT, ACTIVE, CLOSED)
    @Transactional
    public SurveyDTO updateStatus(Long id, String status, String username) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey", id));
        if (!survey.getCreator().getUsername().equals(username)) {
            throw new ForbiddenException("You can only update your own surveys");
        }
        survey.setStatus(SurveyStatus.valueOf(status));
        return mapToDTO(surveyRepository.save(survey));
    }

    // Toggle survey visibility between PUBLIC and PRIVATE
    @Transactional
    public SurveyDTO toggleVisibility(Long id, String username) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey", id));
        if (!survey.getCreator().getUsername().equals(username)) {
            throw new ForbiddenException("You can only update your own surveys");
        }
        survey.setVisibility(survey.getVisibility() == SurveyVisibility.PUBLIC
                ? SurveyVisibility.PRIVATE : SurveyVisibility.PUBLIC);
        return mapToDTO(surveyRepository.save(survey));
    }

    // Delete a survey and all associated data (cascade)
    @Transactional
    public void deleteSurvey(Long id, String username) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey", id));

        if (!survey.getCreator().getUsername().equals(username)) {
            throw new ForbiddenException("You can only delete your own surveys");
        }

        surveyRepository.delete(survey);
    }

    // Map a QuestionDTO to a Question entity with nested response options
    private Question mapToQuestion(QuestionDTO dto, Survey survey) {
        Question question = Question.builder()
                .text(dto.getText())
                .type(QuestionType.valueOf(dto.getType()))
                .questionOrder(dto.getQuestionOrder())
                .required(dto.getRequired() != null ? dto.getRequired() : true)
                .likertMin(dto.getLikertMin())
                .likertMax(dto.getLikertMax())
                .maxTextLength(dto.getMaxTextLength())
                .survey(survey)
                .conditionalQuestionId(dto.getConditionalQuestionId())
                .conditionalAnswer(dto.getConditionalAnswer())
                .build();

        // Add response options for multiple choice questions
        if (dto.getResponseOptions() != null) {
            List<ResponseOption> options = new ArrayList<>();
            for (ResponseOptionDTO optDto : dto.getResponseOptions()) {
                ResponseOption option = ResponseOption.builder()
                        .text(optDto.getText())
                        .optionOrder(optDto.getOptionOrder())
                        .question(question)
                        .build();
                options.add(option);
            }
            question.setResponseOptions(options);
        }

        return question;
    }

    // Map a Survey entity to a SurveyDTO including response count
    private SurveyDTO mapToDTO(Survey survey) {
        List<QuestionDTO> questionDTOs = survey.getQuestions().stream()
                .map(this::mapQuestionToDTO)
                .collect(Collectors.toList());

        long responseCount = surveyResponseRepository.countBySurveyId(survey.getId());

        return SurveyDTO.builder()
                .id(survey.getId())
                .title(survey.getTitle())
                .description(survey.getDescription())
                .startDate(survey.getStartDate())
                .endDate(survey.getEndDate())
                .visibility(survey.getVisibility().name())
                .status(survey.getStatus().name())
                .questions(questionDTOs)
                .creatorUsername(survey.getCreator().getUsername())
                .shareLink(survey.getShareLink())
                .createdAt(survey.getCreatedAt())
                .updatedAt(survey.getUpdatedAt())
                .totalResponses((int) responseCount)
                .build();
    }

    // Map a Question entity to a QuestionDTO
    private QuestionDTO mapQuestionToDTO(Question question) {
        List<ResponseOptionDTO> optionDTOs = question.getResponseOptions().stream()
                .map(opt -> ResponseOptionDTO.builder()
                        .id(opt.getId())
                        .text(opt.getText())
                        .optionOrder(opt.getOptionOrder())
                        .build())
                .collect(Collectors.toList());

        return QuestionDTO.builder()
                .id(question.getId())
                .text(question.getText())
                .type(question.getType().name())
                .questionOrder(question.getQuestionOrder())
                .required(question.getRequired())
                .likertMin(question.getLikertMin())
                .likertMax(question.getLikertMax())
                .maxTextLength(question.getMaxTextLength())
                .responseOptions(optionDTOs)
                .conditionalQuestionId(question.getConditionalQuestionId())
                .conditionalAnswer(question.getConditionalAnswer())
                .build();
    }
}
