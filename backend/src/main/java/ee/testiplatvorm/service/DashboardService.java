package ee.testiplatvorm.service;

import ee.testiplatvorm.controller.dashboard.dto.DashboardDto;
import ee.testiplatvorm.infrastructure.exception.ForbiddenException;
import ee.testiplatvorm.persistence.aiquestion.AiQuestionRepository;
import ee.testiplatvorm.persistence.question.QuestionRepository;
import ee.testiplatvorm.persistence.result.ResultRepository;
import ee.testiplatvorm.persistence.test.TestRepository;
import ee.testiplatvorm.persistence.user.User;
import ee.testiplatvorm.persistence.usertest.UserTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

import static ee.testiplatvorm.Error.NO_PERMISSION_TO_VIEW_DASHBOARD;
import static ee.testiplatvorm.Status.STATUS_ACTIVE;
import static ee.testiplatvorm.Status.STATUS_FAILED;
import static ee.testiplatvorm.Status.STATUS_OPEN;
import static ee.testiplatvorm.Status.STATUS_PASSED;
import static ee.testiplatvorm.Status.STATUS_PENDING;

@Service
@RequiredArgsConstructor
public class DashboardService {

    // "Hiljuti lisandunud" = viimase 7 päeva jooksul loodud
    private static final int RECENT_DAYS = 7;

    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final AiQuestionRepository aiQuestionRepository;
    private final UserTestRepository userTestRepository;
    private final ResultRepository resultRepository;
    private final UserService userService;
    private final CurrentUserService currentUserService;

    public DashboardDto findDashboard() {
        Integer userId = currentUserService.getUserId();
        validateUserCanViewDashboard(userId);
        OffsetDateTime recentFrom = OffsetDateTime.now().minusDays(RECENT_DAYS);

        DashboardDto dashboardDto = new DashboardDto();
        // Määratud, aga veel sooritamata testid — samad, mis "Minu testid" lehel avatud olekus
        dashboardDto.setAssignedTestCount(userTestRepository.countUserTestsBy(userId, STATUS_OPEN.getCode(), STATUS_ACTIVE.getCode()));
        dashboardDto.setRecentTestCount(testRepository.countTestsCreatedAfter(recentFrom));
        dashboardDto.setRecentQuestionCount(questionRepository.countQuestionsCreatedAfter(recentFrom));
        dashboardDto.setPendingAiQuestionCount(aiQuestionRepository.countAiQuestionsBy(STATUS_PENDING.getCode()));
        dashboardDto.setRecentDays(RECENT_DAYS);
        dashboardDto.setCompetenceScores(resultRepository.findCompetenceScores());
        dashboardDto.setAverageScorePercent(resultRepository.findAverageScorePercent());
        dashboardDto.setPassedResultCount(resultRepository.countCompletedResultsBy(STATUS_PASSED.getCode()));
        dashboardDto.setFailedResultCount(resultRepository.countCompletedResultsBy(STATUS_FAILED.getCode()));
        return dashboardDto;
    }

    private void validateUserCanViewDashboard(Integer userId) {
        User user = userService.getValidUserBy(userId);
        String roleName = user.getRole().getName();

        boolean isAllowedToViewDashboard = roleName.equals("ADMIN") || roleName.equals("HALDUR");
        if (!isAllowedToViewDashboard) {
            throw new ForbiddenException(NO_PERMISSION_TO_VIEW_DASHBOARD.getMessage(), NO_PERMISSION_TO_VIEW_DASHBOARD.name());
        }
    }
}
