package ee.testiplatvorm.service;

import ee.testiplatvorm.controller.result.dto.ResultResponseDto;
import ee.testiplatvorm.infrastructure.exception.ForbiddenException;
import ee.testiplatvorm.persistence.result.Result;
import ee.testiplatvorm.persistence.result.ResultMapper;
import ee.testiplatvorm.persistence.result.ResultRepository;
import ee.testiplatvorm.persistence.test.Test;
import ee.testiplatvorm.persistence.usertest.UserTest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static ee.testiplatvorm.Error.NO_RESULT_FOUND;


@Service
@RequiredArgsConstructor
public class ResultService {
    private static final int DECIMAL_POINTS = 2;

    private final ResultRepository resultRepository;
    private final ResultMapper resultMapper;
    private final CurrentUserService currentUserService;
    private final UserService userService;

    public ResultResponseDto findTestResult(Integer userTestId) {
        Result result = resultRepository.findResultByUserTestId(userTestId)
                .orElseThrow(() -> new ForbiddenException(NO_RESULT_FOUND.getMessage(), NO_RESULT_FOUND.name()));
        validateUserCanViewResult(result);

        ResultResponseDto resultResponseDto = resultMapper.toResultResponseDto(result);
        BigDecimal resultScorePercentage = calculateUserResultPercentage(result);
        resultResponseDto.setUserAchievedScorePercentage(resultScorePercentage);
        return resultResponseDto;
    }

    // Tavakasutaja näeb ainult enda tulemust, ADMIN ja HALDUR kõiki.
    // Võõra tulemuse korral sama viga mis puuduva tulemuse korral, et ei saaks ära arvata, kas see on olemas.
    private void validateUserCanViewResult(Result result) {
        Integer userId = currentUserService.getUserId();
        boolean isOwnResult = result.getUserTest().getUser().getId().equals(userId);
        if (isOwnResult) {
            return;
        }

        String roleName = userService.getValidUserBy(userId).getRole().getName();
        boolean isAllowedToViewAllResults = roleName.equals("ADMIN") || roleName.equals("HALDUR");
        if (!isAllowedToViewAllResults) {
            throw new ForbiddenException(NO_RESULT_FOUND.getMessage(), NO_RESULT_FOUND.name());
        }
    }

    public BigDecimal calculateUserResultPercentage(Result result) {
        UserTest userTest = result.getUserTest();
        Test test = userTest.getTest();
        boolean roundScoreUp = test.getRoundScoreUp();
        BigDecimal maxScore = BigDecimal.valueOf(result.getMaxScore());
        BigDecimal userScore = BigDecimal.valueOf(result.getScoreTotal());

        if (maxScore.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }

        BigDecimal userAchievedPercentage = userScore.multiply(BigDecimal.valueOf(100)).divide(maxScore, DECIMAL_POINTS, RoundingMode.HALF_UP);
        BigDecimal userAchievedEndResultPercentage = userAchievedPercentage;

        if (roundScoreUp) {
            userAchievedEndResultPercentage = userAchievedPercentage.setScale(0, RoundingMode.HALF_UP);
        }
        return userAchievedEndResultPercentage;
    }
}
