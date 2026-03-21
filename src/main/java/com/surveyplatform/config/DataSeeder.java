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
        if (userRepository.existsByUsername("vikas")) {
            log.info("Demo data already exists, skipping seed.");
            return;
        }

        log.info("Seeding demo data...");

        // ===== USERS =====
        User vikas = userRepository.save(User.builder()
                .username("vikas")
                .email("vikas.reddy@nci.ie")
                .password(passwordEncoder.encode("Vikas@2026"))
                .fullName("Vikas Reddy Amanagantti")
                .role(Role.ADMIN)
                .build());

        User sarah = userRepository.save(User.builder()
                .username("sarah.johnson")
                .email("sarah.johnson@company.com")
                .password(passwordEncoder.encode("Sarah@2026"))
                .fullName("Sarah Johnson")
                .role(Role.USER)
                .build());

        User michael = userRepository.save(User.builder()
                .username("michael.chen")
                .email("michael.chen@company.com")
                .password(passwordEncoder.encode("Michael@2026"))
                .fullName("Michael Chen")
                .role(Role.USER)
                .build());

        User emma = userRepository.save(User.builder()
                .username("emma.wilson")
                .email("emma.wilson@company.com")
                .password(passwordEncoder.encode("Emma@2026"))
                .fullName("Emma Wilson")
                .role(Role.USER)
                .build());

        User raj = userRepository.save(User.builder()
                .username("raj.patel")
                .email("raj.patel@company.com")
                .password(passwordEncoder.encode("Raj@2026"))
                .fullName("Raj Patel")
                .role(Role.USER)
                .build());

        seedCloudComputingSurvey(vikas, sarah, michael, emma, raj);
        seedRemoteWorkSurvey(vikas, sarah, michael, emma, raj);
        seedStudentFeedbackSurvey(sarah, vikas, michael, emma, raj);
        seedProductLaunchPoll(michael, vikas, sarah, emma, raj);
        seedHealthWellnessSurvey(emma, vikas, sarah, michael, raj);
        seedTechStackPoll(raj, vikas, sarah, michael, emma);
        seedCybersecurityAwarenessSurvey(vikas, sarah, michael, emma, raj);
        seedEventFeedbackDraft(vikas);

        log.info("Demo data seeded. Login: vikas/Vikas@2026, sarah.johnson/Sarah@2026, michael.chen/Michael@2026");
    }

    // ===== Survey 1: Cloud Computing Adoption Survey (by Vikas) =====
    private void seedCloudComputingSurvey(User vikas, User sarah, User michael, User emma, User raj) {
        Survey survey = surveyRepository.save(Survey.builder()
                .title("Cloud Computing Adoption Survey 2026")
                .description("Assessing how organizations are adopting cloud technologies, migration challenges, and preferred providers for strategic planning.")
                .startDate(LocalDateTime.now().minusDays(14))
                .endDate(LocalDateTime.now().plusDays(45))
                .visibility(SurveyVisibility.PUBLIC)
                .status(SurveyStatus.ACTIVE)
                .creator(vikas)
                .shareLink(UUID.randomUUID().toString().substring(0, 8))
                .build());

        Question q1 = questionRepository.save(Question.builder()
                .text("Which cloud provider does your organization primarily use?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(1).required(true)
                .survey(survey).build());

        ResponseOption aws = saveOption("Amazon Web Services (AWS)", 1, q1);
        ResponseOption azure = saveOption("Microsoft Azure", 2, q1);
        ResponseOption gcp = saveOption("Google Cloud Platform", 3, q1);
        ResponseOption multi = saveOption("Multi-cloud strategy", 4, q1);

        Question q2 = questionRepository.save(Question.builder()
                .text("How satisfied are you with your current cloud infrastructure?")
                .type(QuestionType.LIKERT)
                .questionOrder(2).required(true)
                .likertMin(1).likertMax(5)
                .survey(survey).build());

        Question q3 = questionRepository.save(Question.builder()
                .text("What is the biggest challenge you face with cloud adoption?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(3).required(true)
                .survey(survey).build());

        ResponseOption cost = saveOption("Cost management and optimization", 1, q3);
        ResponseOption sec = saveOption("Security and compliance concerns", 2, q3);
        ResponseOption skill = saveOption("Skill gaps in the team", 3, q3);
        ResponseOption migr = saveOption("Legacy system migration complexity", 4, q3);

        Question q4 = questionRepository.save(Question.builder()
                .text("What additional cloud services would benefit your workflow?")
                .type(QuestionType.OPEN_TEXT)
                .questionOrder(4).required(false)
                .maxTextLength(500)
                .survey(survey).build());

        ResponseOption[][] choices = {{aws, cost}, {azure, sec}, {gcp, skill}, {aws, migr}, {multi, cost},
                {aws, sec}, {azure, cost}, {gcp, migr}, {multi, skill}, {aws, cost}};
        int[] ratings = {4, 5, 3, 4, 5, 4, 3, 4, 5, 4};
        User[] respondents = {sarah, michael, emma, raj, null, null, sarah, null, michael, null};
        String[] feedback = {
                "Serverless computing has reduced our operational overhead significantly.",
                "We need better integration between Azure DevOps and third-party monitoring tools.",
                "Container orchestration with Kubernetes needs a simpler learning curve.",
                "Hybrid cloud support for on-premise legacy databases would be valuable.",
                "Auto-scaling policies need more granular control for cost optimization.",
                "", "Better cost dashboards with predictive billing alerts.",
                "Managed machine learning pipelines as a service.",
                "Cross-cloud networking and unified identity management.",
                ""};
        int[] daysAgo = {13, 12, 11, 10, 9, 8, 7, 5, 3, 1};

        for (int i = 0; i < 10; i++) {
            LocalDateTime time = LocalDateTime.now().minusDays(daysAgo[i]).minusHours(i + 2);
            SurveyResponse resp = surveyResponseRepository.save(SurveyResponse.builder()
                    .survey(survey).respondent(respondents[i]).completed(true)
                    .startedAt(time.minusMinutes(4)).submittedAt(time).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q1)
                    .answerValue(choices[i][0].getText()).selectedOptionId(choices[i][0].getId()).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q2)
                    .answerValue(String.valueOf(ratings[i])).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q3)
                    .answerValue(choices[i][1].getText()).selectedOptionId(choices[i][1].getId()).build());
            if (!feedback[i].isEmpty()) {
                answerRepository.save(Answer.builder().surveyResponse(resp).question(q4)
                        .answerValue(feedback[i]).build());
            }
        }

        resultReportRepository.save(ResultReport.builder()
                .survey(survey)
                .title("Cloud Adoption Trends - Q1 2026")
                .summaryData("{\"topProvider\":\"AWS\",\"avgSatisfaction\":4.1,\"topChallenge\":\"Cost management\"}")
                .totalResponses(10).completionRate(92.0).averageTimeSeconds(210L)
                .generatedBy(vikas).build());
    }

    // ===== Survey 2: Remote Work Experience (by Vikas) =====
    private void seedRemoteWorkSurvey(User vikas, User sarah, User michael, User emma, User raj) {
        Survey survey = surveyRepository.save(Survey.builder()
                .title("Remote Work Experience Survey")
                .description("Understanding employee perspectives on remote work policies, productivity, and work-life balance to shape future workplace strategies.")
                .startDate(LocalDateTime.now().minusDays(10))
                .endDate(LocalDateTime.now().plusDays(20))
                .visibility(SurveyVisibility.PUBLIC)
                .status(SurveyStatus.ACTIVE)
                .creator(vikas)
                .shareLink(UUID.randomUUID().toString().substring(0, 8))
                .build());

        Question q1 = questionRepository.save(Question.builder()
                .text("How many days per week do you prefer working remotely?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(1).required(true)
                .survey(survey).build());

        ResponseOption d1 = saveOption("1-2 days (hybrid)", 1, q1);
        ResponseOption d2 = saveOption("3-4 days (mostly remote)", 2, q1);
        ResponseOption d3 = saveOption("Fully remote (5 days)", 3, q1);
        ResponseOption d4 = saveOption("Prefer full-time office", 4, q1);

        Question q2 = questionRepository.save(Question.builder()
                .text("Rate your productivity while working from home")
                .type(QuestionType.LIKERT)
                .questionOrder(2).required(true)
                .likertMin(1).likertMax(5)
                .survey(survey).build());

        Question q3 = questionRepository.save(Question.builder()
                .text("What would improve your remote work setup?")
                .type(QuestionType.OPEN_TEXT)
                .questionOrder(3).required(false)
                .maxTextLength(500)
                .survey(survey).build());

        ResponseOption[] prefs = {d2, d3, d1, d3, d2, d4, d3, d2};
        int[] ratings = {4, 5, 3, 5, 4, 2, 4, 4};
        User[] respondents = {sarah, michael, emma, raj, null, null, sarah, null};
        String[] comments = {
                "A dedicated company-provided monitor and ergonomic chair would make a big difference.",
                "Already fully set up at home, very productive with fewer office distractions.",
                "Better video conferencing tools and reliable VPN access during peak hours.",
                "Home office stipend for internet and electricity costs would be appreciated.",
                "Structured check-ins to avoid feeling isolated from the team.",
                "I find in-person collaboration more effective for brainstorming sessions.",
                "", "Noise-cancelling headphones and a standing desk allowance."};
        int[] daysAgo = {9, 8, 7, 6, 5, 4, 2, 1};

        for (int i = 0; i < 8; i++) {
            LocalDateTime time = LocalDateTime.now().minusDays(daysAgo[i]).minusHours(i + 1);
            SurveyResponse resp = surveyResponseRepository.save(SurveyResponse.builder()
                    .survey(survey).respondent(respondents[i]).completed(true)
                    .startedAt(time.minusMinutes(3)).submittedAt(time).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q1)
                    .answerValue(prefs[i].getText()).selectedOptionId(prefs[i].getId()).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q2)
                    .answerValue(String.valueOf(ratings[i])).build());
            if (!comments[i].isEmpty()) {
                answerRepository.save(Answer.builder().surveyResponse(resp).question(q3)
                        .answerValue(comments[i]).build());
            }
        }
    }

    // ===== Survey 3: Student Course Feedback (by Sarah) =====
    private void seedStudentFeedbackSurvey(User sarah, User vikas, User michael, User emma, User raj) {
        Survey survey = surveyRepository.save(Survey.builder()
                .title("MSc DevOps Course Feedback")
                .description("Collecting student feedback on the Cloud DevSecOps module to improve curriculum, teaching methods, and assessment structure.")
                .startDate(LocalDateTime.now().minusDays(5))
                .endDate(LocalDateTime.now().plusDays(25))
                .visibility(SurveyVisibility.PUBLIC)
                .status(SurveyStatus.ACTIVE)
                .creator(sarah)
                .shareLink(UUID.randomUUID().toString().substring(0, 8))
                .build());

        Question q1 = questionRepository.save(Question.builder()
                .text("How would you rate the overall quality of this module?")
                .type(QuestionType.LIKERT)
                .questionOrder(1).required(true)
                .likertMin(1).likertMax(5)
                .survey(survey).build());

        Question q2 = questionRepository.save(Question.builder()
                .text("Which topic was most valuable for your learning?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(2).required(true)
                .survey(survey).build());

        ResponseOption t1 = saveOption("CI/CD Pipeline Design", 1, q2);
        ResponseOption t2 = saveOption("Container Security with Trivy", 2, q2);
        ResponseOption t3 = saveOption("Infrastructure as Code (Terraform)", 3, q2);
        ResponseOption t4 = saveOption("Static Code Analysis (SAST/SCA)", 4, q2);

        Question q3 = questionRepository.save(Question.builder()
                .text("What topic should be covered in more depth?")
                .type(QuestionType.OPEN_TEXT)
                .questionOrder(3).required(false)
                .maxTextLength(500)
                .survey(survey).build());

        int[] ratings = {5, 4, 5, 4, 3, 5};
        ResponseOption[] topics = {t1, t4, t2, t3, t1, t2};
        User[] respondents = {vikas, michael, emma, raj, null, null};
        String[] feedback = {
                "The hands-on CI/CD pipeline project with GitHub Actions was extremely practical and job-relevant.",
                "SAST tools like SpotBugs and PMD gave great insight into code quality enforcement.",
                "Trivy for container scanning opened my eyes to supply chain security risks.",
                "Terraform modules were well explained but need more multi-cloud examples.",
                "More coverage on Kubernetes security and runtime protection would be helpful.",
                "Excellent balance between theory and practical implementation."};
        int[] daysAgo = {4, 4, 3, 3, 2, 1};

        for (int i = 0; i < 6; i++) {
            LocalDateTime time = LocalDateTime.now().minusDays(daysAgo[i]).minusHours(i * 2 + 1);
            SurveyResponse resp = surveyResponseRepository.save(SurveyResponse.builder()
                    .survey(survey).respondent(respondents[i]).completed(true)
                    .startedAt(time.minusMinutes(5)).submittedAt(time).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q1)
                    .answerValue(String.valueOf(ratings[i])).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q2)
                    .answerValue(topics[i].getText()).selectedOptionId(topics[i].getId()).build());
            if (!feedback[i].isEmpty()) {
                answerRepository.save(Answer.builder().surveyResponse(resp).question(q3)
                        .answerValue(feedback[i]).build());
            }
        }

        resultReportRepository.save(ResultReport.builder()
                .survey(survey)
                .title("Course Feedback Analysis - March 2026")
                .summaryData("{\"avgRating\":4.3,\"topTopic\":\"CI/CD Pipeline Design\",\"students\":6}")
                .totalResponses(6).completionRate(100.0).averageTimeSeconds(180L)
                .generatedBy(sarah).build());
    }

    // ===== Survey 4: Product Launch Readiness Poll (by Michael) =====
    private void seedProductLaunchPoll(User michael, User vikas, User sarah, User emma, User raj) {
        Survey survey = surveyRepository.save(Survey.builder()
                .title("Q2 Product Launch Readiness Poll")
                .description("Quick assessment of team readiness for the upcoming product launch, covering documentation, testing, and marketing preparedness.")
                .startDate(LocalDateTime.now().minusDays(3))
                .endDate(LocalDateTime.now().plusDays(7))
                .visibility(SurveyVisibility.PUBLIC)
                .status(SurveyStatus.ACTIVE)
                .creator(michael)
                .shareLink(UUID.randomUUID().toString().substring(0, 8))
                .build());

        Question q1 = questionRepository.save(Question.builder()
                .text("How confident are you that we are ready for launch?")
                .type(QuestionType.LIKERT)
                .questionOrder(1).required(true)
                .likertMin(1).likertMax(10)
                .survey(survey).build());

        Question q2 = questionRepository.save(Question.builder()
                .text("Which area needs the most attention before launch?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(2).required(true)
                .survey(survey).build());

        ResponseOption a1 = saveOption("Performance and load testing", 1, q2);
        ResponseOption a2 = saveOption("User documentation and guides", 2, q2);
        ResponseOption a3 = saveOption("Marketing and communications", 3, q2);
        ResponseOption a4 = saveOption("Security audit and penetration testing", 4, q2);

        int[] confidence = {8, 6, 9, 7, 5, 8, 7};
        ResponseOption[] areas = {a4, a1, a2, a3, a1, a4, a2};
        User[] respondents = {vikas, sarah, emma, raj, null, null, null};
        int[] daysAgo = {3, 2, 2, 2, 1, 1, 0};

        for (int i = 0; i < 7; i++) {
            LocalDateTime time = LocalDateTime.now().minusDays(daysAgo[i]).minusHours(i + 2);
            SurveyResponse resp = surveyResponseRepository.save(SurveyResponse.builder()
                    .survey(survey).respondent(respondents[i]).completed(true)
                    .startedAt(time.minusMinutes(1)).submittedAt(time).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q1)
                    .answerValue(String.valueOf(confidence[i])).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q2)
                    .answerValue(areas[i].getText()).selectedOptionId(areas[i].getId()).build());
        }
    }

    // ===== Survey 5: Health and Wellness (by Emma) =====
    private void seedHealthWellnessSurvey(User emma, User vikas, User sarah, User michael, User raj) {
        Survey survey = surveyRepository.save(Survey.builder()
                .title("Employee Health and Wellness Check")
                .description("Anonymous survey to understand employee wellbeing, stress levels, and interest in wellness programs to improve workplace support.")
                .startDate(LocalDateTime.now().minusDays(7))
                .endDate(LocalDateTime.now().plusDays(30))
                .visibility(SurveyVisibility.PUBLIC)
                .status(SurveyStatus.ACTIVE)
                .creator(emma)
                .shareLink(UUID.randomUUID().toString().substring(0, 8))
                .build());

        Question q1 = questionRepository.save(Question.builder()
                .text("How would you rate your overall work-related stress level?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(1).required(true)
                .survey(survey).build());

        ResponseOption s1 = saveOption("Low - I feel balanced and relaxed", 1, q1);
        ResponseOption s2 = saveOption("Moderate - Manageable but noticeable", 2, q1);
        ResponseOption s3 = saveOption("High - Frequently stressed", 3, q1);
        ResponseOption s4 = saveOption("Very high - Affecting my health", 4, q1);

        Question q2 = questionRepository.save(Question.builder()
                .text("Rate your satisfaction with current wellness benefits")
                .type(QuestionType.LIKERT)
                .questionOrder(2).required(true)
                .likertMin(1).likertMax(5)
                .survey(survey).build());

        Question q3 = questionRepository.save(Question.builder()
                .text("Which wellness initiative would you value most?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(3).required(true)
                .survey(survey).build());

        ResponseOption w1 = saveOption("Mental health counselling sessions", 1, q3);
        ResponseOption w2 = saveOption("Gym membership or fitness classes", 2, q3);
        ResponseOption w3 = saveOption("Flexible working hours", 3, q3);
        ResponseOption w4 = saveOption("Mindfulness and meditation programs", 4, q3);

        ResponseOption[] stress = {s2, s1, s3, s2, s1, s2, s3, s2, s1};
        int[] ratings = {3, 4, 2, 3, 5, 3, 2, 4, 4};
        ResponseOption[] wellness = {w3, w2, w1, w3, w4, w1, w3, w2, w4};
        User[] respondents = {vikas, sarah, michael, raj, null, null, null, null, null};
        int[] daysAgo = {6, 6, 5, 5, 4, 3, 3, 2, 1};

        for (int i = 0; i < 9; i++) {
            LocalDateTime time = LocalDateTime.now().minusDays(daysAgo[i]).minusHours(i + 1);
            SurveyResponse resp = surveyResponseRepository.save(SurveyResponse.builder()
                    .survey(survey).respondent(respondents[i]).completed(true)
                    .startedAt(time.minusMinutes(3)).submittedAt(time).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q1)
                    .answerValue(stress[i].getText()).selectedOptionId(stress[i].getId()).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q2)
                    .answerValue(String.valueOf(ratings[i])).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q3)
                    .answerValue(wellness[i].getText()).selectedOptionId(wellness[i].getId()).build());
        }
    }

    // ===== Survey 6: Tech Stack Poll (by Raj) =====
    private void seedTechStackPoll(User raj, User vikas, User sarah, User michael, User emma) {
        Survey survey = surveyRepository.save(Survey.builder()
                .title("Preferred Tech Stack for 2026 Projects")
                .description("Poll to determine which programming languages, frameworks, and databases the team wants to adopt for upcoming projects.")
                .startDate(LocalDateTime.now().minusDays(4))
                .endDate(LocalDateTime.now().plusDays(14))
                .visibility(SurveyVisibility.PUBLIC)
                .status(SurveyStatus.ACTIVE)
                .creator(raj)
                .shareLink(UUID.randomUUID().toString().substring(0, 8))
                .build());

        Question q1 = questionRepository.save(Question.builder()
                .text("Which backend framework do you prefer for new projects?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(1).required(true)
                .survey(survey).build());

        ResponseOption b1 = saveOption("Spring Boot (Java)", 1, q1);
        ResponseOption b2 = saveOption("Django / FastAPI (Python)", 2, q1);
        ResponseOption b3 = saveOption("Express / NestJS (Node.js)", 3, q1);
        ResponseOption b4 = saveOption("Go (Gin / Echo)", 4, q1);

        Question q2 = questionRepository.save(Question.builder()
                .text("Which frontend framework should we standardize on?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(2).required(true)
                .survey(survey).build());

        ResponseOption f1 = saveOption("React", 1, q2);
        ResponseOption f2 = saveOption("Angular", 2, q2);
        ResponseOption f3 = saveOption("Vue.js", 3, q2);
        ResponseOption f4 = saveOption("Next.js", 4, q2);

        ResponseOption[] backends = {b1, b2, b3, b1, b4, b2};
        ResponseOption[] frontends = {f1, f4, f1, f2, f1, f3};
        User[] respondents = {vikas, sarah, michael, emma, null, null};
        int[] daysAgo = {3, 3, 2, 2, 1, 1};

        for (int i = 0; i < 6; i++) {
            LocalDateTime time = LocalDateTime.now().minusDays(daysAgo[i]).minusHours(i * 3 + 1);
            SurveyResponse resp = surveyResponseRepository.save(SurveyResponse.builder()
                    .survey(survey).respondent(respondents[i]).completed(true)
                    .startedAt(time.minusMinutes(1)).submittedAt(time).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q1)
                    .answerValue(backends[i].getText()).selectedOptionId(backends[i].getId()).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q2)
                    .answerValue(frontends[i].getText()).selectedOptionId(frontends[i].getId()).build());
        }
    }

    // ===== Survey 7: Cybersecurity Awareness (by Vikas) =====
    private void seedCybersecurityAwarenessSurvey(User vikas, User sarah, User michael, User emma, User raj) {
        Survey survey = surveyRepository.save(Survey.builder()
                .title("Cybersecurity Awareness Assessment")
                .description("Evaluating team knowledge on security best practices, phishing awareness, and secure coding habits to identify training needs.")
                .startDate(LocalDateTime.now().minusDays(6))
                .endDate(LocalDateTime.now().plusDays(15))
                .visibility(SurveyVisibility.PUBLIC)
                .status(SurveyStatus.ACTIVE)
                .creator(vikas)
                .shareLink(UUID.randomUUID().toString().substring(0, 8))
                .build());

        Question q1 = questionRepository.save(Question.builder()
                .text("How often do you update your passwords for work accounts?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(1).required(true)
                .survey(survey).build());

        ResponseOption p1 = saveOption("Every 30 days", 1, q1);
        ResponseOption p2 = saveOption("Every 90 days", 2, q1);
        ResponseOption p3 = saveOption("Every 6 months", 3, q1);
        ResponseOption p4 = saveOption("Only when required by system", 4, q1);

        Question q2 = questionRepository.save(Question.builder()
                .text("Rate your confidence in identifying phishing emails")
                .type(QuestionType.LIKERT)
                .questionOrder(2).required(true)
                .likertMin(1).likertMax(5)
                .survey(survey).build());

        Question q3 = questionRepository.save(Question.builder()
                .text("Do you use multi-factor authentication on all work accounts?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(3).required(true)
                .survey(survey).build());

        ResponseOption m1 = saveOption("Yes, on all accounts", 1, q3);
        ResponseOption m2 = saveOption("Yes, on most accounts", 2, q3);
        ResponseOption m3 = saveOption("Only on email", 3, q3);
        ResponseOption m4 = saveOption("No, I haven't set it up", 4, q3);

        ResponseOption[] passwords = {p2, p1, p3, p2, p4, p1, p2};
        int[] confidence = {4, 5, 3, 4, 2, 5, 3};
        ResponseOption[] mfa = {m1, m1, m2, m1, m3, m1, m4};
        User[] respondents = {sarah, michael, emma, raj, null, null, null};
        int[] daysAgo = {5, 5, 4, 4, 3, 2, 1};

        for (int i = 0; i < 7; i++) {
            LocalDateTime time = LocalDateTime.now().minusDays(daysAgo[i]).minusHours(i * 2 + 3);
            SurveyResponse resp = surveyResponseRepository.save(SurveyResponse.builder()
                    .survey(survey).respondent(respondents[i]).completed(true)
                    .startedAt(time.minusMinutes(4)).submittedAt(time).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q1)
                    .answerValue(passwords[i].getText()).selectedOptionId(passwords[i].getId()).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q2)
                    .answerValue(String.valueOf(confidence[i])).build());
            answerRepository.save(Answer.builder().surveyResponse(resp).question(q3)
                    .answerValue(mfa[i].getText()).selectedOptionId(mfa[i].getId()).build());
        }

        resultReportRepository.save(ResultReport.builder()
                .survey(survey)
                .title("Security Awareness Baseline - March 2026")
                .summaryData("{\"avgPhishingConfidence\":3.7,\"mfaAdoption\":\"71%\",\"passwordCompliance\":\"57%\"}")
                .totalResponses(7).completionRate(100.0).averageTimeSeconds(165L)
                .generatedBy(vikas).build());
    }

    // ===== Survey 8: Event Feedback Draft (by Vikas) =====
    private void seedEventFeedbackDraft(User vikas) {
        Survey survey = surveyRepository.save(Survey.builder()
                .title("Annual Tech Conference Feedback")
                .description("Post-event survey to gather attendee feedback on speakers, sessions, networking opportunities, and venue logistics.")
                .startDate(LocalDateTime.now().plusDays(5))
                .endDate(LocalDateTime.now().plusDays(30))
                .visibility(SurveyVisibility.PRIVATE)
                .status(SurveyStatus.DRAFT)
                .creator(vikas)
                .shareLink(UUID.randomUUID().toString().substring(0, 8))
                .build());

        questionRepository.save(Question.builder()
                .text("Rate the overall quality of the conference sessions")
                .type(QuestionType.LIKERT)
                .questionOrder(1).required(true)
                .likertMin(1).likertMax(5)
                .survey(survey).build());

        Question q2 = questionRepository.save(Question.builder()
                .text("Which session track was most relevant to your work?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .questionOrder(2).required(true)
                .survey(survey).build());

        saveOption("Cloud Architecture and DevOps", 1, q2);
        saveOption("AI and Machine Learning", 2, q2);
        saveOption("Cybersecurity and Compliance", 3, q2);
        saveOption("Leadership and Team Management", 4, q2);

        questionRepository.save(Question.builder()
                .text("What topics would you like to see at next year's conference?")
                .type(QuestionType.OPEN_TEXT)
                .questionOrder(3).required(false)
                .maxTextLength(500)
                .survey(survey).build());
    }

    private ResponseOption saveOption(String text, int order, Question question) {
        return responseOptionRepository.save(ResponseOption.builder()
                .text(text).optionOrder(order).question(question).build());
    }
}
