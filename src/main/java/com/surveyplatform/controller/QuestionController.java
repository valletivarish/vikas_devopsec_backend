package com.surveyplatform.controller;

import com.surveyplatform.dto.QuestionDTO;
import com.surveyplatform.dto.ResponseOptionDTO;
import com.surveyplatform.exception.ResourceNotFoundException;
import com.surveyplatform.model.Question;
import com.surveyplatform.model.QuestionType;
import com.surveyplatform.model.ResponseOption;
import com.surveyplatform.model.Survey;
import com.surveyplatform.repository.QuestionRepository;
import com.surveyplatform.repository.ResponseOptionRepository;
import com.surveyplatform.repository.SurveyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// REST controller for managing individual questions within a survey
// Provides CRUD operations for questions and their response options
@RestController
@RequestMapping("/api/questions")
@Tag(name = "Questions", description = "Question CRUD endpoints")
public class QuestionController {

    private final QuestionRepository questionRepository;
    private final SurveyRepository surveyRepository;
    private final ResponseOptionRepository responseOptionRepository;

    // Constructor injection for repository dependencies
    public QuestionController(QuestionRepository questionRepository,
                              SurveyRepository surveyRepository,
                              ResponseOptionRepository responseOptionRepository) {
        this.questionRepository = questionRepository;
        this.surveyRepository = surveyRepository;
        this.responseOptionRepository = responseOptionRepository;
    }

    // Get all questions for a specific survey
    @GetMapping("/survey/{surveyId}")
    @Operation(summary = "Get all questions for a survey")
    public ResponseEntity<List<QuestionDTO>> getQuestionsBySurvey(@PathVariable Long surveyId) {
        List<Question> questions = questionRepository.findBySurveyIdOrderByQuestionOrderAsc(surveyId);
        List<QuestionDTO> dtos = questions.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Get a single question by ID
    @GetMapping("/{id}")
    @Operation(summary = "Get a question by ID")
    public ResponseEntity<QuestionDTO> getQuestionById(@PathVariable Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", id));
        return ResponseEntity.ok(mapToDTO(question));
    }

    // Create a new question for a survey
    @PostMapping("/survey/{surveyId}")
    @Operation(summary = "Add a question to a survey")
    public ResponseEntity<QuestionDTO> createQuestion(@PathVariable Long surveyId,
                                                       @Valid @RequestBody QuestionDTO dto) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey", surveyId));

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

        // Add response options if provided
        if (dto.getResponseOptions() != null) {
            List<ResponseOption> options = new ArrayList<>();
            for (ResponseOptionDTO optDto : dto.getResponseOptions()) {
                options.add(ResponseOption.builder()
                        .text(optDto.getText())
                        .optionOrder(optDto.getOptionOrder())
                        .question(question)
                        .build());
            }
            question.setResponseOptions(options);
        }

        Question saved = questionRepository.save(question);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(saved));
    }

    // Update an existing question
    @PutMapping("/{id}")
    @Operation(summary = "Update a question")
    public ResponseEntity<QuestionDTO> updateQuestion(@PathVariable Long id,
                                                       @Valid @RequestBody QuestionDTO dto) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", id));

        question.setText(dto.getText());
        question.setType(QuestionType.valueOf(dto.getType()));
        question.setQuestionOrder(dto.getQuestionOrder());
        question.setRequired(dto.getRequired() != null ? dto.getRequired() : true);
        question.setLikertMin(dto.getLikertMin());
        question.setLikertMax(dto.getLikertMax());
        question.setMaxTextLength(dto.getMaxTextLength());
        question.setConditionalQuestionId(dto.getConditionalQuestionId());
        question.setConditionalAnswer(dto.getConditionalAnswer());

        // Replace response options
        question.getResponseOptions().clear();
        if (dto.getResponseOptions() != null) {
            for (ResponseOptionDTO optDto : dto.getResponseOptions()) {
                question.getResponseOptions().add(ResponseOption.builder()
                        .text(optDto.getText())
                        .optionOrder(optDto.getOptionOrder())
                        .question(question)
                        .build());
            }
        }

        Question saved = questionRepository.save(question);
        return ResponseEntity.ok(mapToDTO(saved));
    }

    // Delete a question by ID
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a question")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", id));
        questionRepository.delete(question);
        return ResponseEntity.noContent().build();
    }

    // Map Question entity to QuestionDTO
    private QuestionDTO mapToDTO(Question question) {
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
