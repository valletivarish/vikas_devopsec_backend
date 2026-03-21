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
        User demoUser = userRepository.save(User.builder()
                .username("demouser")
                .email("demo@surveyplatform.com")
                .password(passwordEncoder.encode("Demo@1234"))
                .fullName("Demo User")
                .role(Role.USER)
                .build());

        User adminUser = userRepository.save(User.builder()
                .username("admin")
                .email("admin@surveyplatform.com")
                .password(passwordEncoder.encode("Admin@1234"))
                .fullName("Platform Admin")
                .role(Role.ADMIN)
                .build());

        User analystUser = userRepository.save(User.builder()
                .username("analyst")
                .email("analyst@surveyplatform.com")
                .password(passwordEncoder.encode("Analyst@1234"))
                .fullName("Data Analyst")
                .role(Role.USER)
                .build());

        // ===== DEMO USER: Customer Satisfaction Survey =====
        seedDemoUserData(demoUser, adminUser, analystUser);

        // ===== ADMIN USER: Employee Engagement Poll + Feature Prioritization =====
        seedAdminUserData(adminUser, demoUser, analystUser);

        // ===== ANALYST USER: Quick Polls =====
        seedAnalystUserData(analystUser, demoUser, adminUser);

        log.info("Demo data seeded successfully. Login: demouser/Demo@1234, admin/Admin@1234, analyst/Analyst@1234");
    }

    private void seedDemoUserData(User demoUser, User admin, User analyst) {
        // Survey 1: Customer Satisfaction Survey
        Survey survey1 = surveyRepository.save(Survey.builder()
                .title("Customer Satisfaction Survey")
                .description("Help us improve our services by sharing your experience.")
                .startDate(LocalDateTime.now().minusDays(7))
                .endDate(LocalDateTime.now().plusDays(30))
                .visibility(SurveyVisibility.PUBLIC)
                .status(SurveyStatus.ACTIVE)
                .creator(demoUser)
                .shareLink(UUID.randomUUID().toString().substring(0, 8))
                .build());

        Question q1 = questionRepository.save(Question.builder()
                .text("How satisfied are you with our service?")
                .type(QuestionType.LIKERT)
                .questionOrder(1).required(true)
                .likertMin(1).likertMax(5)
                .survey(survey1).build());

        Question q2 = questionRepository.save(Question.builder()
                .text("Which feature do you use most?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(2).required(true)
                .survey(survey1).build());

        ResponseOption opt1 = saveOption("Dashboard Analytics", 1, q2);
        ResponseOption opt2 = saveOption("Survey Builder", 2, q2);
        ResponseOption opt3 = saveOption("Result Reports", 3, q2);
        ResponseOption opt4 = saveOption("Forecasting", 4, q2);

        Question q3 = questionRepository.save(Question.builder()
                .text("Any suggestions for improvement?")
                .type(QuestionType.OPEN_TEXT)
                .questionOrder(3).required(false)
                .maxTextLength(500)
                .survey(survey1).build());

        // Responses for Survey 1
        int[] ratings = {5, 4, 4, 3, 5, 4, 3, 5};
        ResponseOption[] choices = {opt1, opt2, opt3, opt4, opt1, opt2, opt3, opt1};
        User[] respondents = {admin, analyst, demoUser, null, null, admin, analyst, null};
        String[] feedbacks = {"Great platform!", "Would like PDF export.", "Very intuitive.", "Add dark mode!", "Excellent forecasting.", "", "Improve mobile.", "Satisfied overall."};
        int[] daysAgo = {6, 5, 5, 4, 3, 2, 1, 0};

        for (int i = 0; i < 8; i++) {
            LocalDateTime time = LocalDateTime.now().minusDays(daysAgo[i]).minusHours(i);
            SurveyResponse resp = surveyResponseRepository.save(SurveyResponse.builder()
                    .survey(survey1).respondent(respondents[i]).completed(true)
                    .startedAt(time.minusMinutes(2)).submittedAt(time).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q1).answerValue(String.valueOf(ratings[i])).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q2).answerValue(choices[i].getText()).selectedOptionId(choices[i].getId()).build());
            if (!feedbacks[i].isEmpty()) {
                answerRepository.save(Answer.builder().surveyResponse(resp).question(q3).answerValue(feedbacks[i]).build());
            }
        }

        // Survey 2: Website Feedback (short poll)
        Survey survey2 = surveyRepository.save(Survey.builder()
                .title("Website Feedback Poll")
                .description("Quick poll about our website redesign.")
                .startDate(LocalDateTime.now().minusDays(2))
                .endDate(LocalDateTime.now().plusDays(10))
                .visibility(SurveyVisibility.PUBLIC)
                .status(SurveyStatus.ACTIVE)
                .creator(demoUser)
                .shareLink(UUID.randomUUID().toString().substring(0, 8))
                .build());

        Question q4 = questionRepository.save(Question.builder()
                .text("Do you like the new website design?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(1).required(true)
                .survey(survey2).build());

        ResponseOption y = saveOption("Yes, it looks great", 1, q4);
        saveOption("No, prefer the old one", 2, q4);
        ResponseOption m = saveOption("It's okay", 3, q4);

        // A few responses
        addSimpleResponse(survey2, q4, y, admin, 1);
        addSimpleResponse(survey2, q4, y, null, 0);
        addSimpleResponse(survey2, q4, m, analyst, 0);

        // Report for Survey 1
        resultReportRepository.save(ResultReport.builder()
                .survey(survey1)
                .title("Customer Satisfaction - Weekly Report")
                .summaryData("{\"avgSatisfaction\":4.1,\"topFeature\":\"Dashboard Analytics\"}")
                .totalResponses(8).completionRate(87.5).averageTimeSeconds(145L)
                .generatedBy(demoUser).build());
    }

    private void seedAdminUserData(User adminUser, User demo, User analyst) {
        // Survey: Employee Engagement Poll
        Survey survey = surveyRepository.save(Survey.builder()
                .title("Employee Engagement Poll")
                .description("Quick poll to gauge team morale and engagement levels.")
                .startDate(LocalDateTime.now().minusDays(3))
                .endDate(LocalDateTime.now().plusDays(14))
                .visibility(SurveyVisibility.PUBLIC)
                .status(SurveyStatus.ACTIVE)
                .creator(adminUser)
                .shareLink(UUID.randomUUID().toString().substring(0, 8))
                .build());

        Question q1 = questionRepository.save(Question.builder()
                .text("How would you rate your work-life balance?")
                .type(QuestionType.LIKERT)
                .questionOrder(1).required(true)
                .likertMin(1).likertMax(10)
                .survey(survey).build());

        Question q2 = questionRepository.save(Question.builder()
                .text("What motivates you the most at work?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(2).required(true)
                .survey(survey).build());

        ResponseOption opt1 = saveOption("Team Collaboration", 1, q2);
        ResponseOption opt2 = saveOption("Career Growth", 2, q2);
        ResponseOption opt3 = saveOption("Compensation", 3, q2);
        ResponseOption opt4 = saveOption("Flexible Schedule", 4, q2);

        int[] ratings = {8, 6, 9, 7, 5};
        ResponseOption[] motivators = {opt2, opt1, opt4, opt3, opt2};
        User[] respondents = {demo, adminUser, analyst, null, null};
        int[] daysAgo = {2, 2, 1, 1, 0};

        for (int i = 0; i < 5; i++) {
            LocalDateTime time = LocalDateTime.now().minusDays(daysAgo[i]).minusHours(i + 3);
            SurveyResponse resp = surveyResponseRepository.save(SurveyResponse.builder()
                    .survey(survey).respondent(respondents[i]).completed(true)
                    .startedAt(time.minusMinutes(1)).submittedAt(time).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q1).answerValue(String.valueOf(ratings[i])).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q2).answerValue(motivators[i].getText()).selectedOptionId(motivators[i].getId()).build());
        }

        // Survey: Product Feature Prioritization (DRAFT)
        Survey survey2 = surveyRepository.save(Survey.builder()
                .title("Product Feature Prioritization")
                .description("Vote on which features we should build next quarter.")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(21))
                .visibility(SurveyVisibility.PRIVATE)
                .status(SurveyStatus.DRAFT)
                .creator(adminUser)
                .shareLink(UUID.randomUUID().toString().substring(0, 8))
                .build());

        Question q3 = questionRepository.save(Question.builder()
                .text("Which feature should we prioritize?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(1).required(true)
                .survey(survey2).build());

        saveOption("Real-time Collaboration", 1, q3);
        saveOption("Advanced Analytics", 2, q3);
        saveOption("Mobile App", 3, q3);
        saveOption("API Integrations", 4, q3);

        // Report
        resultReportRepository.save(ResultReport.builder()
                .survey(survey)
                .title("Employee Engagement - March 2026")
                .summaryData("{\"avgWorkLifeBalance\":7.2,\"topMotivator\":\"Career Growth\"}")
                .totalResponses(5).completionRate(100.0).averageTimeSeconds(98L)
                .generatedBy(adminUser).build());
    }

    private void seedAnalystUserData(User analystUser, User demo, User admin) {
        // Poll 1: Lunch Preference Poll
        Survey poll1 = surveyRepository.save(Survey.builder()
                .title("Team Lunch Preference Poll")
                .description("Vote for this Friday's team lunch.")
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(3))
                .visibility(SurveyVisibility.PUBLIC)
                .status(SurveyStatus.ACTIVE)
                .creator(analystUser)
                .shareLink(UUID.randomUUID().toString().substring(0, 8))
                .build());

        Question pq1 = questionRepository.save(Question.builder()
                .text("Where should we go for lunch?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(1).required(true)
                .survey(poll1).build());

        ResponseOption po1 = saveOption("Italian", 1, pq1);
        ResponseOption po2 = saveOption("Japanese", 2, pq1);
        saveOption("Mexican", 3, pq1);
        ResponseOption po4 = saveOption("Indian", 4, pq1);

        addSimpleResponse(poll1, pq1, po2, demo, 1);
        addSimpleResponse(poll1, pq1, po1, admin, 0);
        addSimpleResponse(poll1, pq1, po4, null, 0);
        addSimpleResponse(poll1, pq1, po2, null, 0);

        // Poll 2: Meeting Time Poll
        Survey poll2 = surveyRepository.save(Survey.builder()
                .title("Weekly Standup Time Poll")
                .description("Vote for the best time for our weekly standup meeting.")
                .startDate(LocalDateTime.now().minusDays(2))
                .endDate(LocalDateTime.now().plusDays(5))
                .visibility(SurveyVisibility.PUBLIC)
                .status(SurveyStatus.ACTIVE)
                .creator(analystUser)
                .shareLink(UUID.randomUUID().toString().substring(0, 8))
                .build());

        Question pq2 = questionRepository.save(Question.builder()
                .text("What time works best for you?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(1).required(true)
                .survey(poll2).build());

        ResponseOption t1 = saveOption("9:00 AM", 1, pq2);
        ResponseOption t2 = saveOption("10:00 AM", 2, pq2);
        ResponseOption t3 = saveOption("11:00 AM", 3, pq2);
        saveOption("2:00 PM", 4, pq2);

        addSimpleResponse(poll2, pq2, t2, demo, 2);
        addSimpleResponse(poll2, pq2, t1, admin, 1);
        addSimpleResponse(poll2, pq2, t2, analystUser, 1);
        addSimpleResponse(poll2, pq2, t3, null, 0);
        addSimpleResponse(poll2, pq2, t2, null, 0);

        // Survey: Data Tools Usage Survey
        Survey survey = surveyRepository.save(Survey.builder()
                .title("Data Tools Usage Survey")
                .description("Understanding which analytics tools our team uses and their effectiveness.")
                .startDate(LocalDateTime.now().minusDays(5))
                .endDate(LocalDateTime.now().plusDays(20))
                .visibility(SurveyVisibility.PUBLIC)
                .status(SurveyStatus.ACTIVE)
                .creator(analystUser)
                .shareLink(UUID.randomUUID().toString().substring(0, 8))
                .build());

        Question sq1 = questionRepository.save(Question.builder()
                .text("Rate your satisfaction with current analytics tools")
                .type(QuestionType.LIKERT)
                .questionOrder(1).required(true)
                .likertMin(1).likertMax(5)
                .survey(survey).build());

        Question sq2 = questionRepository.save(Question.builder()
                .text("Which tool do you use most frequently?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(2).required(true)
                .survey(survey).build());

        ResponseOption to1 = saveOption("Excel / Sheets", 1, sq2);
        ResponseOption to2 = saveOption("Tableau", 2, sq2);
        ResponseOption to3 = saveOption("Python / Jupyter", 3, sq2);
        ResponseOption to4 = saveOption("Power BI", 4, sq2);

        Question sq3 = questionRepository.save(Question.builder()
                .text("What analytics capability are you missing?")
                .type(QuestionType.OPEN_TEXT)
                .questionOrder(3).required(false)
                .maxTextLength(500)
                .survey(survey).build());

        // Responses
        int[] toolRatings = {4, 3, 5, 4};
        ResponseOption[] tools = {to3, to1, to2, to4};
        User[] respondents = {demo, admin, null, null};
        String[] comments = {"Need better real-time dashboards.", "More automation.", "", "Integration with Slack."};

        for (int i = 0; i < 4; i++) {
            LocalDateTime time = LocalDateTime.now().minusDays(4 - i).minusHours(i * 2);
            SurveyResponse resp = surveyResponseRepository.save(SurveyResponse.builder()
                    .survey(survey).respondent(respondents[i]).completed(true)
                    .startedAt(time.minusMinutes(3)).submittedAt(time).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(sq1).answerValue(String.valueOf(toolRatings[i])).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(sq2).answerValue(tools[i].getText()).selectedOptionId(tools[i].getId()).build());
            if (!comments[i].isEmpty()) {
                answerRepository.save(Answer.builder().surveyResponse(resp).question(sq3).answerValue(comments[i]).build());
            }
        }
    }

    private ResponseOption saveOption(String text, int order, Question question) {
        return responseOptionRepository.save(ResponseOption.builder()
                .text(text).optionOrder(order).question(question).build());
    }

    private void addSimpleResponse(Survey survey, Question question, ResponseOption option, User respondent, int daysAgo) {
        LocalDateTime time = LocalDateTime.now().minusDays(daysAgo).minusHours((int) (Math.random() * 8));
        SurveyResponse resp = surveyResponseRepository.save(SurveyResponse.builder()
                .survey(survey).respondent(respondent).completed(true)
                .startedAt(time.minusMinutes(1)).submittedAt(time).build());
        answerRepository.save(Answer.builder()
                .surveyResponse(resp).question(question)
                .answerValue(option.getText()).selectedOptionId(option.getId()).build());
    }
}
