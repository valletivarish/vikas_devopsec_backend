package com.surveyplatform.config;

import com.surveyplatform.model.*;
import com.surveyplatform.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final ResponseOptionRepository responseOptionRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final AnswerRepository answerRepository;
    private final ResultReportRepository resultReportRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      SurveyRepository surveyRepository,
                      QuestionRepository questionRepository,
                      ResponseOptionRepository responseOptionRepository,
                      SurveyResponseRepository surveyResponseRepository,
                      AnswerRepository answerRepository,
                      ResultReportRepository resultReportRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
        this.responseOptionRepository = responseOptionRepository;
        this.surveyResponseRepository = surveyResponseRepository;
        this.answerRepository = answerRepository;
        this.resultReportRepository = resultReportRepository;
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

        // ===== USERS =====
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
        analystUser = userRepository.save(analystUser);

        // ===== SURVEY 1: Customer Satisfaction =====
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
        q1 = questionRepository.save(q1);

        Question q2 = Question.builder()
                .text("Which feature do you use most?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(2)
                .required(true)
                .survey(survey1)
                .build();
        q2 = questionRepository.save(q2);

        ResponseOption opt1 = responseOptionRepository.save(ResponseOption.builder()
                .text("Dashboard Analytics").optionOrder(1).question(q2).build());
        ResponseOption opt2 = responseOptionRepository.save(ResponseOption.builder()
                .text("Survey Builder").optionOrder(2).question(q2).build());
        ResponseOption opt3 = responseOptionRepository.save(ResponseOption.builder()
                .text("Result Reports").optionOrder(3).question(q2).build());
        ResponseOption opt4 = responseOptionRepository.save(ResponseOption.builder()
                .text("Forecasting").optionOrder(4).question(q2).build());

        Question q3 = Question.builder()
                .text("Any suggestions for improvement?")
                .type(QuestionType.OPEN_TEXT)
                .questionOrder(3)
                .required(false)
                .maxTextLength(500)
                .survey(survey1)
                .build();
        q3 = questionRepository.save(q3);

        // ===== SURVEY 2: Employee Engagement =====
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
        q4 = questionRepository.save(q4);

        Question q5 = Question.builder()
                .text("What motivates you the most at work?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(2)
                .required(true)
                .survey(survey2)
                .build();
        q5 = questionRepository.save(q5);

        ResponseOption opt5 = responseOptionRepository.save(ResponseOption.builder()
                .text("Team Collaboration").optionOrder(1).question(q5).build());
        ResponseOption opt6 = responseOptionRepository.save(ResponseOption.builder()
                .text("Career Growth").optionOrder(2).question(q5).build());
        ResponseOption opt7 = responseOptionRepository.save(ResponseOption.builder()
                .text("Compensation").optionOrder(3).question(q5).build());
        ResponseOption opt8 = responseOptionRepository.save(ResponseOption.builder()
                .text("Flexible Schedule").optionOrder(4).question(q5).build());

        // ===== SURVEY 3: Product Feature Prioritization =====
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

        // ===== SURVEY RESPONSES for Survey 1 (Customer Satisfaction) =====
        seedSurvey1Responses(survey1, q1, q2, q3, opt1, opt2, opt3, opt4,
                adminUser, analystUser, demoUser);

        // ===== SURVEY RESPONSES for Survey 2 (Employee Engagement) =====
        seedSurvey2Responses(survey2, q4, q5, opt5, opt6, opt7, opt8,
                demoUser, adminUser, analystUser);

        // ===== RESULT REPORTS =====
        resultReportRepository.save(ResultReport.builder()
                .survey(survey1)
                .title("Customer Satisfaction - Weekly Report")
                .summaryData("{\"avgSatisfaction\":4.1,\"topFeature\":\"Dashboard Analytics\",\"responseRate\":\"85%\",\"npsScore\":72}")
                .totalResponses(8)
                .completionRate(87.5)
                .averageTimeSeconds(145L)
                .generatedBy(demoUser)
                .build());

        resultReportRepository.save(ResultReport.builder()
                .survey(survey2)
                .title("Employee Engagement - March 2026")
                .summaryData("{\"avgWorkLifeBalance\":7.2,\"topMotivator\":\"Career Growth\",\"responseRate\":\"92%\",\"engagementScore\":78}")
                .totalResponses(5)
                .completionRate(100.0)
                .averageTimeSeconds(98L)
                .generatedBy(adminUser)
                .build());

        log.info("Demo data seeded: 3 users, 3 surveys, 13 responses, 2 reports. Login: demouser/Demo@1234, admin/Admin@1234, analyst/Analyst@1234");
    }

    private void seedSurvey1Responses(Survey survey, Question q1, Question q2, Question q3,
                                       ResponseOption opt1, ResponseOption opt2,
                                       ResponseOption opt3, ResponseOption opt4,
                                       User admin, User analyst, User demo) {
        String[] feedbacks = {
                "Great platform, love the analytics!",
                "Would be nice to have PDF export for reports.",
                "The survey builder is very intuitive.",
                "Please add dark mode!",
                "Excellent forecasting feature.",
                "",
                "Mobile experience could be improved.",
                "Very satisfied overall."
        };
        int[] ratings = {5, 4, 4, 3, 5, 4, 3, 5};
        ResponseOption[] choices = {opt1, opt2, opt3, opt4, opt1, opt2, opt3, opt1};
        User[] respondents = {admin, analyst, demo, null, null, admin, analyst, null};
        int[] daysAgo = {6, 5, 5, 4, 3, 2, 1, 0};

        for (int i = 0; i < 8; i++) {
            LocalDateTime submittedTime = LocalDateTime.now().minusDays(daysAgo[i]).minusHours(i);
            SurveyResponse response = SurveyResponse.builder()
                    .survey(survey)
                    .respondent(respondents[i])
                    .completed(true)
                    .startedAt(submittedTime.minusMinutes(2))
                    .submittedAt(submittedTime)
                    .build();
            response = surveyResponseRepository.save(response);

            answerRepository.save(Answer.builder()
                    .surveyResponse(response)
                    .question(q1)
                    .answerValue(String.valueOf(ratings[i]))
                    .build());

            answerRepository.save(Answer.builder()
                    .surveyResponse(response)
                    .question(q2)
                    .answerValue(choices[i].getText())
                    .selectedOptionId(choices[i].getId())
                    .build());

            if (!feedbacks[i].isEmpty()) {
                answerRepository.save(Answer.builder()
                        .surveyResponse(response)
                        .question(q3)
                        .answerValue(feedbacks[i])
                        .build());
            }
        }
    }

    private void seedSurvey2Responses(Survey survey, Question q4, Question q5,
                                       ResponseOption opt5, ResponseOption opt6,
                                       ResponseOption opt7, ResponseOption opt8,
                                       User demo, User admin, User analyst) {
        int[] balanceRatings = {8, 6, 9, 7, 5};
        ResponseOption[] motivators = {opt6, opt5, opt8, opt7, opt6};
        User[] respondents = {demo, admin, analyst, null, null};
        int[] daysAgo = {2, 2, 1, 1, 0};

        for (int i = 0; i < 5; i++) {
            LocalDateTime submittedTime = LocalDateTime.now().minusDays(daysAgo[i]).minusHours(i + 3);
            SurveyResponse response = SurveyResponse.builder()
                    .survey(survey)
                    .respondent(respondents[i])
                    .completed(true)
                    .startedAt(submittedTime.minusMinutes(1))
                    .submittedAt(submittedTime)
                    .build();
            response = surveyResponseRepository.save(response);

            answerRepository.save(Answer.builder()
                    .surveyResponse(response)
                    .question(q4)
                    .answerValue(String.valueOf(balanceRatings[i]))
                    .build());

            answerRepository.save(Answer.builder()
                    .surveyResponse(response)
                    .question(q5)
                    .answerValue(motivators[i].getText())
                    .selectedOptionId(motivators[i].getId())
                    .build());
        }
    }
}
