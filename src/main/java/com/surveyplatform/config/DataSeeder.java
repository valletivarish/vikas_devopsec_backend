package com.surveyplatform.config;

import com.surveyplatform.model.*;
import com.surveyplatform.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final ResponseOptionRepository responseOptionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      SurveyRepository surveyRepository,
                      QuestionRepository questionRepository,
                      ResponseOptionRepository responseOptionRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
        this.responseOptionRepository = responseOptionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByUsername("demouser")) {
            log.info("Demo data already exists, skipping seed.");
            return;
        }

        log.info("Seeding demo data...");

        User demoUser = User.builder()
                .username("demouser")
                .email("demo@surveyplatform.com")
                .password(passwordEncoder.encode("Demo@1234"))
                .fullName("Demo User")
                .role(Role.USER)
                .build();
        demoUser = userRepository.save(demoUser);

        User adminUser = User.builder()
                .username("admin")
                .email("admin@surveyplatform.com")
                .password(passwordEncoder.encode("Admin@1234"))
                .fullName("Platform Admin")
                .role(Role.ADMIN)
                .build();
        adminUser = userRepository.save(adminUser);

        User analystUser = User.builder()
                .username("analyst")
                .email("analyst@surveyplatform.com")
                .password(passwordEncoder.encode("Analyst@1234"))
                .fullName("Data Analyst")
                .role(Role.USER)
                .build();
        userRepository.save(analystUser);

        Survey survey1 = Survey.builder()
                .title("Customer Satisfaction Survey")
                .description("Help us improve our services by sharing your experience.")
                .startDate(LocalDateTime.now().minusDays(7))
                .endDate(LocalDateTime.now().plusDays(30))
                .visibility(SurveyVisibility.PUBLIC)
                .status(SurveyStatus.ACTIVE)
                .creator(demoUser)
                .shareLink(UUID.randomUUID().toString().substring(0, 8))
                .build();
        survey1 = surveyRepository.save(survey1);

        Question q1 = Question.builder()
                .text("How satisfied are you with our service?")
                .type(QuestionType.LIKERT)
                .questionOrder(1)
                .required(true)
                .likertMin(1)
                .likertMax(5)
                .survey(survey1)
                .build();
        questionRepository.save(q1);

        Question q2 = Question.builder()
                .text("Which feature do you use most?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(2)
                .required(true)
                .survey(survey1)
                .build();
        q2 = questionRepository.save(q2);

        responseOptionRepository.save(ResponseOption.builder()
                .text("Dashboard Analytics").optionOrder(1).question(q2).build());
        responseOptionRepository.save(ResponseOption.builder()
                .text("Survey Builder").optionOrder(2).question(q2).build());
        responseOptionRepository.save(ResponseOption.builder()
                .text("Result Reports").optionOrder(3).question(q2).build());
        responseOptionRepository.save(ResponseOption.builder()
                .text("Forecasting").optionOrder(4).question(q2).build());

        Question q3 = Question.builder()
                .text("Any suggestions for improvement?")
                .type(QuestionType.OPEN_TEXT)
                .questionOrder(3)
                .required(false)
                .maxTextLength(500)
                .survey(survey1)
                .build();
        questionRepository.save(q3);

        Survey survey2 = Survey.builder()
                .title("Employee Engagement Poll")
                .description("Quick poll to gauge team morale and engagement levels.")
                .startDate(LocalDateTime.now().minusDays(3))
                .endDate(LocalDateTime.now().plusDays(14))
                .visibility(SurveyVisibility.PUBLIC)
                .status(SurveyStatus.ACTIVE)
                .creator(demoUser)
                .shareLink(UUID.randomUUID().toString().substring(0, 8))
                .build();
        survey2 = surveyRepository.save(survey2);

        Question q4 = Question.builder()
                .text("How would you rate your work-life balance?")
                .type(QuestionType.LIKERT)
                .questionOrder(1)
                .required(true)
                .likertMin(1)
                .likertMax(10)
                .survey(survey2)
                .build();
        questionRepository.save(q4);

        Question q5 = Question.builder()
                .text("What motivates you the most at work?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(2)
                .required(true)
                .survey(survey2)
                .build();
        q5 = questionRepository.save(q5);

        responseOptionRepository.save(ResponseOption.builder()
                .text("Team Collaboration").optionOrder(1).question(q5).build());
        responseOptionRepository.save(ResponseOption.builder()
                .text("Career Growth").optionOrder(2).question(q5).build());
        responseOptionRepository.save(ResponseOption.builder()
                .text("Compensation").optionOrder(3).question(q5).build());
        responseOptionRepository.save(ResponseOption.builder()
                .text("Flexible Schedule").optionOrder(4).question(q5).build());

        Survey survey3 = Survey.builder()
                .title("Product Feature Prioritization")
                .description("Vote on which features we should build next quarter.")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(21))
                .visibility(SurveyVisibility.PRIVATE)
                .status(SurveyStatus.DRAFT)
                .creator(adminUser)
                .shareLink(UUID.randomUUID().toString().substring(0, 8))
                .build();
        survey3 = surveyRepository.save(survey3);

        Question q6 = Question.builder()
                .text("Which feature should we prioritize?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(1)
                .required(true)
                .survey(survey3)
                .build();
        q6 = questionRepository.save(q6);

        responseOptionRepository.save(ResponseOption.builder()
                .text("Real-time Collaboration").optionOrder(1).question(q6).build());
        responseOptionRepository.save(ResponseOption.builder()
                .text("Advanced Analytics").optionOrder(2).question(q6).build());
        responseOptionRepository.save(ResponseOption.builder()
                .text("Mobile App").optionOrder(3).question(q6).build());
        responseOptionRepository.save(ResponseOption.builder()
                .text("API Integrations").optionOrder(4).question(q6).build());

        log.info("Demo data seeded successfully. Login with demouser/Demo@1234, admin/Admin@1234, or analyst/Analyst@1234");
    }
}
