package ee.testiplatvorm.service;

import ee.testiplatvorm.controller.result.dto.ResultResponseDto;
import ee.testiplatvorm.infrastructure.exception.ForbiddenException;
import ee.testiplatvorm.persistence.Role;
import ee.testiplatvorm.persistence.result.Result;
import ee.testiplatvorm.persistence.result.ResultMapper;
import ee.testiplatvorm.persistence.result.ResultRepository;
import ee.testiplatvorm.persistence.user.User;
import ee.testiplatvorm.persistence.usertest.UserTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// calculateUserResultPercentage on puhas arvutus – mock'e kasutavad ainult findTestResult testid
@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    private static final int USER_TEST_ID = 1;
    private static final int RESULT_OWNER_ID = 7;

    @Mock
    private ResultRepository resultRepository;
    @Mock
    private ResultMapper resultMapper;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private UserService userService;

    @InjectMocks
    private ResultService resultService;

    // Abimeetod: tulemus, kus kasutaja sai scoreTotal punkti maxScore-st
    private Result resultWith(int scoreTotal, int maxScore, boolean roundScoreUp) {
        ee.testiplatvorm.persistence.test.Test test = new ee.testiplatvorm.persistence.test.Test();
        test.setRoundScoreUp(roundScoreUp);
        UserTest userTest = new UserTest();
        userTest.setTest(test);
        Result result = new Result();
        result.setUserTest(userTest);
        result.setScoreTotal(scoreTotal);
        result.setMaxScore(maxScore);
        return result;
    }

    // NB! isEqualTo võrdleb BigDecimal-i koos komakohtadega: "70.00" ei ole sama mis "70"

    @Test
    void calculateUserResultPercentage_sevenOutOfTen_returns70WithTwoDecimals() {
        Result result = resultWith(7, 10, false);

        BigDecimal percentage = resultService.calculateUserResultPercentage(result);

        assertThat(percentage).isEqualTo(new BigDecimal("70.00"));
    }

    @Test
    void calculateUserResultPercentage_twoOutOfThreeWithoutRounding_returns66point67() {
        Result result = resultWith(2, 3, false);

        BigDecimal percentage = resultService.calculateUserResultPercentage(result);

        assertThat(percentage).isEqualTo(new BigDecimal("66.67"));
    }

    @Test
    void calculateUserResultPercentage_twoOutOfThreeWithRounding_returns67() {
        Result result = resultWith(2, 3, true);

        BigDecimal percentage = resultService.calculateUserResultPercentage(result);

        assertThat(percentage).isEqualTo(new BigDecimal("67"));
    }

    @Test
    void calculateUserResultPercentage_halfWithRounding_roundsUp() {
        // 1/8 = 12.5% → ümardatakse üles 13-ks
        Result result = resultWith(1, 8, true);

        BigDecimal percentage = resultService.calculateUserResultPercentage(result);

        assertThat(percentage).isEqualTo(new BigDecimal("13"));
    }

    @Test
    void calculateUserResultPercentage_allPoints_returns100() {
        Result result = resultWith(10, 10, false);

        BigDecimal percentage = resultService.calculateUserResultPercentage(result);

        assertThat(percentage).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void calculateUserResultPercentage_zeroPoints_returns0() {
        Result result = resultWith(0, 10, false);

        BigDecimal percentage = resultService.calculateUserResultPercentage(result);

        assertThat(percentage).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void calculateUserResultPercentage_maxScoreZero_returns100() {
        // Päriselt seda olukorda tekkida ei saa (testil on alati vähemalt 1 küsimus, @NotEmpty),
        // test dokumenteerib, et nulliga jagamise asemel tagastatakse 100
        Result result = resultWith(0, 0, false);

        BigDecimal percentage = resultService.calculateUserResultPercentage(result);

        assertThat(percentage).isEqualTo(new BigDecimal("100"));
    }

    // --- findTestResult: kes tohib tulemust vaadata ---

    // Tulemus, mis kuulub kasutajale 7 (7/10 punkti)
    private Result mockResultOwnedByUser7() {
        User resultOwner = new User();
        resultOwner.setId(RESULT_OWNER_ID);
        Result result = resultWith(7, 10, false);
        result.getUserTest().setUser(resultOwner);
        when(resultRepository.findResultByUserTestId(USER_TEST_ID)).thenReturn(Optional.of(result));
        return result;
    }

    // Ainult testides, kus tulemust tohib näha – keelatud juhul mapperini ei jõuta
    private void mockResultMapper(Result result) {
        when(resultMapper.toResultResponseDto(result)).thenReturn(new ResultResponseDto());
    }

    // Sisse loginud kasutaja, kes EI OLE tulemuse omanik
    private void mockOtherUserWithRole(String roleName) {
        Role role = new Role();
        role.setName(roleName);
        User user = new User();
        user.setRole(role);
        when(currentUserService.getUserId()).thenReturn(5);
        when(userService.getValidUserBy(5)).thenReturn(user);
    }

    @Test
    void findTestResult_userViewsOwnResult_returnsResultWithPercentage() {
        // Arrange – sisse loginud kasutaja on tulemuse omanik, rolli pole vaja kontrollida
        Result result = mockResultOwnedByUser7();
        mockResultMapper(result);
        when(currentUserService.getUserId()).thenReturn(RESULT_OWNER_ID);

        // Act
        ResultResponseDto resultResponseDto = resultService.findTestResult(USER_TEST_ID);

        // Assert
        assertThat(resultResponseDto.getUserAchievedScorePercentage()).isEqualTo(new BigDecimal("70.00"));
    }

    @Test
    void findTestResult_userViewsOtherUsersResult_throwsForbiddenException() {
        // Arrange
        mockResultOwnedByUser7();
        mockOtherUserWithRole("KASUTAJA");

        // Act + Assert – sama vastus mis "tulemust ei leitud", et ei saaks ära arvata, kas võõras tulemus on olemas
        assertThatThrownBy(() -> resultService.findTestResult(USER_TEST_ID))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Tulemust ei leitud");

        // Võõra tulemuse andmeid ei teisendatud vastuseks
        verify(resultMapper, never()).toResultResponseDto(any());
    }

    @Test
    void findTestResult_adminViewsOtherUsersResult_returnsResult() {
        // Arrange
        Result result = mockResultOwnedByUser7();
        mockResultMapper(result);
        mockOtherUserWithRole("ADMIN");

        // Act
        ResultResponseDto resultResponseDto = resultService.findTestResult(USER_TEST_ID);

        // Assert
        assertThat(resultResponseDto.getUserAchievedScorePercentage()).isEqualTo(new BigDecimal("70.00"));
    }

    @Test
    void findTestResult_haldurViewsOtherUsersResult_returnsResult() {
        // Arrange
        Result result = mockResultOwnedByUser7();
        mockResultMapper(result);
        mockOtherUserWithRole("HALDUR");

        // Act
        ResultResponseDto resultResponseDto = resultService.findTestResult(USER_TEST_ID);

        // Assert
        assertThat(resultResponseDto).isNotNull();
    }

    @Test
    void findTestResult_resultDoesNotExist_throwsForbiddenException() {
        // Arrange
        when(resultRepository.findResultByUserTestId(99)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> resultService.findTestResult(99))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Tulemust ei leitud");

        verify(resultMapper, never()).toResultResponseDto(any());
    }

}
