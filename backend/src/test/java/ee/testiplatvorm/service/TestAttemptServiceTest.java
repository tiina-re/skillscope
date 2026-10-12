package ee.testiplatvorm.service;

import ee.testiplatvorm.controller.result.dto.ResultResponseDto;
import ee.testiplatvorm.controller.testattempt.dto.SubmittedAnswersDto;
import ee.testiplatvorm.infrastructure.exception.ForbiddenException;
import ee.testiplatvorm.persistence.question.Question;
import ee.testiplatvorm.persistence.questionanswer.QuestionAnswerMapper;
import ee.testiplatvorm.persistence.questionanswer.QuestionAnswerRepository;
import ee.testiplatvorm.persistence.result.Result;
import ee.testiplatvorm.persistence.result.ResultMapper;
import ee.testiplatvorm.persistence.result.ResultRepository;
import ee.testiplatvorm.persistence.testquestion.TestQuestion;
import ee.testiplatvorm.persistence.testquestion.TestQuestionMapper;
import ee.testiplatvorm.persistence.testquestion.TestQuestionRepository;
import ee.testiplatvorm.persistence.usertest.UserTest;
import ee.testiplatvorm.persistence.usertest.UserTestMapper;
import ee.testiplatvorm.persistence.usertest.UserTestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestAttemptServiceTest {

    private static final int USER_ID = 5;
    private static final int TEST_ID = 1;

    @Mock
    private UserTestRepository userTestRepository;
    @Mock
    private UserTestMapper userTestMapper;
    @Mock
    private TestQuestionRepository testQuestionRepository;
    @Mock
    private TestQuestionMapper testQuestionMapper;
    @Mock
    private QuestionAnswerMapper questionAnswerMapper;
    @Mock
    private QuestionAnswerRepository questionAnswerRepository;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private ResultRepository resultRepository;
    @Mock
    private ResultMapper resultMapper;

    // @Spy = päris ResultService, mitte mock. Protsendi arvutus on puhas arvutus (testitud ResultServiceTest-is),
    // nii et läbimise kontroll (PASSED/FAILED) kasutab päris ümardamise loogikat.
    @Spy
    private ResultService resultService = new ResultService(null, null, null, null);

    @InjectMocks
    private TestAttemptService testAttemptService;

    // --- Abimeetodid ---

    // Kasutajal on testile avatud määramine; testi läbimisprotsent ja ümardamise seade on antud
    private UserTest mockOpenAssignment(int passPercent, boolean roundScoreUp) {
        ee.testiplatvorm.persistence.test.Test test = new ee.testiplatvorm.persistence.test.Test();
        test.setPassPercent(BigDecimal.valueOf(passPercent));
        test.setRoundScoreUp(roundScoreUp);
        UserTest userTest = new UserTest();
        userTest.setTest(test);
        userTest.setStatus("O");

        when(currentUserService.getUserId()).thenReturn(USER_ID);
        when(userTestRepository.getValidUserTestBy(USER_ID, TEST_ID, "O", "A", "A")).thenReturn(Optional.of(userTest));
        when(resultMapper.toResultResponseDto(any())).thenReturn(new ResultResponseDto());
        return userTest;
    }

    private Question question(int questionId, int score) {
        Question question = new Question();
        question.setId(questionId);
        question.setScore(score);
        return question;
    }

    // Testis on need küsimused, antud järjekorras
    private void mockTestQuestions(Question... questions) {
        List<TestQuestion> testQuestions = new ArrayList<>();
        for (Question question : questions) {
            TestQuestion testQuestion = new TestQuestion();
            testQuestion.setQuestion(question);
            testQuestions.add(testQuestion);
        }
        when(testQuestionRepository.findQuestionsBy(TEST_ID)).thenReturn(testQuestions);
    }

    // Küsimuse õiged vastused. Stub'i ainult küsimustele, millele kasutaja vastas –
    // vastamata küsimuse puhul teenus õigeid vastuseid ei küsi.
    private void mockCorrectAnswers(int questionId, Integer... correctAnswerIds) {
        when(questionAnswerRepository.findCorrectAnswerIdsBy(questionId, "A")).thenReturn(List.of(correctAnswerIds));
    }

    private SubmittedAnswersDto answer(int questionId, Integer... answerIds) {
        return new SubmittedAnswersDto(questionId, List.of(answerIds));
    }

    // Esitab vastused ja püüab kinni salvestatud tulemuse
    private Result submitAndCaptureResult(List<SubmittedAnswersDto> submittedAnswers) {
        testAttemptService.submitTest(TEST_ID, submittedAnswers);
        ArgumentCaptor<Result> resultCaptor = ArgumentCaptor.forClass(Result.class);
        verify(resultRepository).save(resultCaptor.capture());
        return resultCaptor.getValue();
    }

    // --- Õigused ja olek ---

    @Test
    void submitTest_noOpenAssignment_throwsForbiddenException() {
        // Arrange
        when(currentUserService.getUserId()).thenReturn(USER_ID);
        when(userTestRepository.getValidUserTestBy(USER_ID, TEST_ID, "O", "A", "A")).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> testAttemptService.submitTest(TEST_ID, List.of()))
                .isInstanceOf(ForbiddenException.class);

        verify(resultRepository, never()).save(any());
        verify(userTestRepository, never()).save(any());
    }

    @Test
    void submitTest_validSubmission_closesUserTest() {
        // Arrange
        UserTest userTest = mockOpenAssignment(50, false);
        mockTestQuestions(question(1, 1));
        mockCorrectAnswers(1, 11);

        // Act
        testAttemptService.submitTest(TEST_ID, List.of(answer(1, 11)));

        // Assert – suletud määramist ei saa uuesti esitada
        assertThat(userTest.getStatus()).isEqualTo("C");
        verify(userTestRepository).save(userTest);
    }

    // --- Punktid ---

    @Test
    void submitTest_allAnswersCorrect_scoreEqualsMaxScore() {
        // Arrange
        mockOpenAssignment(50, false);
        mockTestQuestions(question(1, 2), question(2, 3));
        mockCorrectAnswers(1, 11);
        mockCorrectAnswers(2, 21, 22);

        // Act
        ResultResponseDto resultResponseDto = testAttemptService.submitTest(TEST_ID, List.of(answer(1, 11), answer(2, 21, 22)));

        // Assert
        ArgumentCaptor<Result> resultCaptor = ArgumentCaptor.forClass(Result.class);
        verify(resultRepository).save(resultCaptor.capture());
        Result result = resultCaptor.getValue();
        assertThat(result.getScoreTotal()).isEqualTo(5);
        assertThat(result.getMaxScore()).isEqualTo(5);
        assertThat(resultResponseDto.getUserAchievedScorePercentage()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void submitTest_singleChoiceWrongAnswer_givesZeroPoints() {
        // Arrange
        mockOpenAssignment(50, false);
        mockTestQuestions(question(1, 2));
        mockCorrectAnswers(1, 11);

        // Act
        Result result = submitAndCaptureResult(List.of(answer(1, 12)));

        // Assert
        assertThat(result.getScoreTotal()).isEqualTo(0);
    }

    @Test
    void submitTest_multipleChoiceOnlySomeCorrectSelected_givesZeroPoints() {
        // Arrange
        mockOpenAssignment(50, false);
        mockTestQuestions(question(2, 3));
        mockCorrectAnswers(2, 21, 22);

        // Act
        Result result = submitAndCaptureResult(List.of(answer(2, 21)));

        // Assert
        assertThat(result.getScoreTotal()).isEqualTo(0);
    }

    @Test
    void submitTest_multipleChoiceAllCorrectPlusOneWrong_givesZeroPoints() {
        // Arrange
        mockOpenAssignment(50, false);
        mockTestQuestions(question(2, 3));
        mockCorrectAnswers(2, 21, 22);

        // Act
        Result result = submitAndCaptureResult(List.of(answer(2, 21, 22, 23)));

        // Assert
        assertThat(result.getScoreTotal()).isEqualTo(0);
    }

    @Test
    void submitTest_multipleChoiceCorrectAnswersInDifferentOrder_givesFullPoints() {
        // Arrange
        mockOpenAssignment(50, false);
        mockTestQuestions(question(2, 3));
        mockCorrectAnswers(2, 21, 22);

        // Act
        Result result = submitAndCaptureResult(List.of(answer(2, 22, 21)));

        // Assert
        assertThat(result.getScoreTotal()).isEqualTo(3);
    }

    @Test
    void submitTest_questionsWithDifferentScores_countsEachQuestionsOwnScore() {
        // Arrange – küsimus 1 annab 2 punkti, küsimus 2 annab 5 punkti
        mockOpenAssignment(50, false);
        mockTestQuestions(question(1, 2), question(2, 5));
        mockCorrectAnswers(1, 11);
        mockCorrectAnswers(2, 21);

        // Act – küsimus 1 vale, küsimus 2 õige
        Result result = submitAndCaptureResult(List.of(answer(1, 12), answer(2, 21)));

        // Assert
        assertThat(result.getMaxScore()).isEqualTo(7);
        assertThat(result.getScoreTotal()).isEqualTo(5);
    }

    // --- Vastatud küsimuste loendus ---

    @Test
    void submitTest_emptySelection_notCountedAsAnswered() {
        // Arrange
        mockOpenAssignment(50, false);
        mockTestQuestions(question(1, 2), question(2, 3));
        mockCorrectAnswers(2, 21);

        // Act – küsimusele 1 saadeti tühi valik
        Result result = submitAndCaptureResult(List.of(answer(1), answer(2, 21)));

        // Assert
        assertThat(result.getQuestionsAnswered()).isEqualTo(1);
        assertThat(result.getScoreTotal()).isEqualTo(3);
    }

    @Test
    void submitTest_questionMissingFromSubmission_notCountedAsAnswered() {
        // Arrange
        mockOpenAssignment(50, false);
        mockTestQuestions(question(1, 2), question(2, 3));
        mockCorrectAnswers(2, 21);

        // Act – küsimust 1 päringus üldse ei ole
        Result result = submitAndCaptureResult(List.of(answer(2, 21)));

        // Assert
        assertThat(result.getQuestionsAnswered()).isEqualTo(1);
        assertThat(result.getScoreTotal()).isEqualTo(3);
    }

    @Test
    void submitTest_threeQuestionsOneAnswered_totalQuestionsIsThree() {
        // Arrange
        mockOpenAssignment(50, false);
        mockTestQuestions(question(1, 1), question(2, 1), question(3, 1));
        mockCorrectAnswers(1, 11);

        // Act
        Result result = submitAndCaptureResult(List.of(answer(1, 11)));

        // Assert – totalQuestions tuleb testi küsimustest, mitte vastuste arvust
        assertThat(result.getTotalQuestions()).isEqualTo(3);
        assertThat(result.getQuestionsAnswered()).isEqualTo(1);
    }

    @Test
    void submitTest_answerToQuestionNotInTest_isIgnored() {
        // Arrange – testis on ainult küsimus 1
        mockOpenAssignment(50, false);
        mockTestQuestions(question(1, 2));
        mockCorrectAnswers(1, 11);

        // Act – lisaks saadetakse vastus küsimusele 99, mida selles testis pole
        Result result = submitAndCaptureResult(List.of(answer(1, 11), answer(99, 991)));

        // Assert
        assertThat(result.getScoreTotal()).isEqualTo(2);
        assertThat(result.getMaxScore()).isEqualTo(2);
        assertThat(result.getQuestionsAnswered()).isEqualTo(1);
        verify(questionAnswerRepository, never()).findCorrectAnswerIdsBy(eq(99), any());
    }

    // --- Läbimine ---

    @Test
    void submitTest_resultExactlyPassPercent_passed() {
        // Arrange – 7/10 = 70%, läbimiseks vaja 70%
        mockOpenAssignment(70, false);
        mockTestQuestions(question(1, 7), question(2, 3));
        mockCorrectAnswers(1, 11);
        mockCorrectAnswers(2, 21);

        // Act
        Result result = submitAndCaptureResult(List.of(answer(1, 11), answer(2, 22)));

        // Assert
        assertThat(result.getStatus()).isEqualTo("P");
    }

    @Test
    void submitTest_resultBelowPassPercent_failed() {
        // Arrange – 6/10 = 60%, läbimiseks vaja 70%
        mockOpenAssignment(70, false);
        mockTestQuestions(question(1, 6), question(2, 4));
        mockCorrectAnswers(1, 11);
        mockCorrectAnswers(2, 21);

        // Act
        Result result = submitAndCaptureResult(List.of(answer(1, 11), answer(2, 22)));

        // Assert
        assertThat(result.getStatus()).isEqualTo("F");
    }

    @Test
    void submitTest_twoThirdsWithRoundingAndPassPercent67_passed() {
        // Arrange – 2/3 = 66.67% → ümardatakse 67-ks, läbimiseks vaja 67%
        mockOpenAssignment(67, true);
        mockTestQuestions(question(1, 2), question(2, 1));
        mockCorrectAnswers(1, 11);
        mockCorrectAnswers(2, 21);

        // Act
        Result result = submitAndCaptureResult(List.of(answer(1, 11), answer(2, 22)));

        // Assert
        assertThat(result.getStatus()).isEqualTo("P");
    }

    @Test
    void submitTest_twoThirdsWithoutRoundingAndPassPercent67_failed() {
        // Arrange – 2/3 = 66.67% ilma ümardamiseta, läbimiseks vaja 67%
        mockOpenAssignment(67, false);
        mockTestQuestions(question(1, 2), question(2, 1));
        mockCorrectAnswers(1, 11);
        mockCorrectAnswers(2, 21);

        // Act
        Result result = submitAndCaptureResult(List.of(answer(1, 11), answer(2, 22)));

        // Assert
        assertThat(result.getStatus()).isEqualTo("F");
    }

}
