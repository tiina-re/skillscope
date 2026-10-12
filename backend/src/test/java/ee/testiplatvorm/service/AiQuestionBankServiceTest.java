package ee.testiplatvorm.service;

import ee.testiplatvorm.controller.aiquestion.dto.AiQuestionBankAnswerDto;
import ee.testiplatvorm.controller.aiquestion.dto.AiQuestionBankDto;
import ee.testiplatvorm.controller.aiquestion.dto.AiQuestionReviewRequestDto;
import ee.testiplatvorm.infrastructure.exception.BadRequestException;
import ee.testiplatvorm.infrastructure.exception.ForbiddenException;
import ee.testiplatvorm.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.testiplatvorm.persistence.Role;
import ee.testiplatvorm.persistence.aiquestion.AiQuestion;
import ee.testiplatvorm.persistence.aiquestion.AiQuestionMapper;
import ee.testiplatvorm.persistence.aiquestion.AiQuestionRepository;
import ee.testiplatvorm.persistence.aiquestionanswer.AiQuestionAnswer;
import ee.testiplatvorm.persistence.aiquestionanswer.AiQuestionAnswerMapper;
import ee.testiplatvorm.persistence.aiquestionanswer.AiQuestionAnswerRepository;
import ee.testiplatvorm.persistence.question.Question;
import ee.testiplatvorm.persistence.question.QuestionRepository;
import ee.testiplatvorm.persistence.questionanswer.QuestionAnswer;
import ee.testiplatvorm.persistence.questionanswer.QuestionAnswerRepository;
import ee.testiplatvorm.persistence.questiontype.QuestionType;
import ee.testiplatvorm.persistence.questiontype.QuestionTypeRepository;
import ee.testiplatvorm.persistence.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiQuestionBankServiceTest {

    private static final int USER_ID = 5;
    private static final int AI_QUESTION_AUTHOR_ID = 7;
    private static final int AI_QUESTION_ID = 1;

    @Mock
    private AiQuestionRepository aiQuestionRepository;
    @Mock
    private AiQuestionMapper aiQuestionMapper;
    @Mock
    private AiQuestionAnswerRepository aiQuestionAnswerRepository;
    @Mock
    private AiQuestionAnswerMapper aiQuestionAnswerMapper;
    @Mock
    private QuestionTypeRepository questionTypeRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private QuestionAnswerRepository questionAnswerRepository;
    @Mock
    private UserService userService;
    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AiQuestionBankService aiQuestionBankService;

    // --- Abimeetodid ---

    private void mockCurrentUserWithRole(String roleName) {
        Role role = new Role();
        role.setName(roleName);
        User user = new User();
        user.setRole(role);
        when(currentUserService.getUserId()).thenReturn(USER_ID);
        when(userService.getValidUserBy(USER_ID)).thenReturn(user);
    }

    private static QuestionType questionType(int questionTypeId, String name) {
        QuestionType questionType = new QuestionType();
        questionType.setId(questionTypeId);
        questionType.setName(name);
        return questionType;
    }

    // Ülevaatust ootav AI küsimus, mille lõi kasutaja 7
    private AiQuestion mockPendingAiQuestion(QuestionType questionType) {
        AiQuestion aiQuestion = new AiQuestion();
        aiQuestion.setId(AI_QUESTION_ID);
        aiQuestion.setTitle("Mis on JVM?");
        aiQuestion.setQuestionTypeId(questionType.getId());
        aiQuestion.setStatus("P");
        aiQuestion.setCreatedBy(AI_QUESTION_AUTHOR_ID);
        when(aiQuestionRepository.findById(AI_QUESTION_ID)).thenReturn(Optional.of(aiQuestion));
        return aiQuestion;
    }

    private List<AiQuestionAnswer> mockAiQuestionAnswers() {
        AiQuestionAnswer correctAnswer = new AiQuestionAnswer();
        correctAnswer.setAnswerText("Java virtuaalmasin");
        correctAnswer.setIsCorrect(true);
        AiQuestionAnswer wrongAnswer = new AiQuestionAnswer();
        wrongAnswer.setAnswerText("Java versioon");
        wrongAnswer.setIsCorrect(false);
        List<AiQuestionAnswer> aiQuestionAnswers = List.of(correctAnswer, wrongAnswer);
        when(aiQuestionAnswerRepository.findAnswersBy(AI_QUESTION_ID)).thenReturn(aiQuestionAnswers);
        return aiQuestionAnswers;
    }

    private static AiQuestionReviewRequestDto review(boolean approved) {
        AiQuestionReviewRequestDto aiQuestionReviewRequestDto = new AiQuestionReviewRequestDto();
        aiQuestionReviewRequestDto.setApproved(approved);
        aiQuestionReviewRequestDto.setScore(4);
        aiQuestionReviewRequestDto.setFeedback("Hea küsimus");
        return aiQuestionReviewRequestDto;
    }

    // --- Ülevaatus ---

    @Test
    void reviewAiQuestion_approved_copiesQuestionWithAnswersToQuestionBank() {
        // Arrange
        mockCurrentUserWithRole("ADMIN");
        QuestionType singleChoice = questionType(1, "SINGLE_CHOICE");
        AiQuestion aiQuestion = mockPendingAiQuestion(singleChoice);
        mockAiQuestionAnswers();
        when(questionTypeRepository.findById(1)).thenReturn(Optional.of(singleChoice));
        User aiQuestionAuthor = new User();
        when(userService.getValidUserBy(AI_QUESTION_AUTHOR_ID)).thenReturn(aiQuestionAuthor);

        // Act
        aiQuestionBankService.reviewAiQuestion(AI_QUESTION_ID, review(true));

        // Assert – AI küsimus on kinnitatud
        assertThat(aiQuestion.getStatus()).isEqualTo("A");
        assertThat(aiQuestion.getIsGood()).isTrue();

        // Küsimuste panka lisati aktiivne küsimus, mille looja on AI küsimuse looja
        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(questionCaptor.capture());
        Question question = questionCaptor.getValue();
        assertThat(question.getTitle()).isEqualTo("Mis on JVM?");
        assertThat(question.getStatus()).isEqualTo("A");
        assertThat(question.getQuestionType()).isEqualTo(singleChoice);
        assertThat(question.getCreatedBy()).isEqualTo(aiQuestionAuthor);
        assertThat(question.getScore()).isEqualTo(10);

        // Vastused kopeeriti ja seoti uue küsimusega
        ArgumentCaptor<QuestionAnswer> questionAnswerCaptor = ArgumentCaptor.forClass(QuestionAnswer.class);
        verify(questionAnswerRepository, times(2)).save(questionAnswerCaptor.capture());
        List<QuestionAnswer> questionAnswers = questionAnswerCaptor.getAllValues();
        assertThat(questionAnswers).extracting("answerText").containsExactly("Java virtuaalmasin", "Java versioon");
        assertThat(questionAnswers).extracting("correctChoice").containsExactly(true, false);
        assertThat(questionAnswers).extracting("status").containsOnly("A");
        assertThat(questionAnswers).extracting("question").containsOnly(question);
    }

    @Test
    void reviewAiQuestion_approvedTrueFalse_copiedQuestionScoreIsFive() {
        // Arrange
        mockCurrentUserWithRole("HALDUR");
        QuestionType trueFalse = questionType(3, "TRUE_FALSE");
        mockPendingAiQuestion(trueFalse);
        mockAiQuestionAnswers();
        when(questionTypeRepository.findById(3)).thenReturn(Optional.of(trueFalse));
        when(userService.getValidUserBy(AI_QUESTION_AUTHOR_ID)).thenReturn(new User());

        // Act
        aiQuestionBankService.reviewAiQuestion(AI_QUESTION_ID, review(true));

        // Assert
        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(questionCaptor.capture());
        assertThat(questionCaptor.getValue().getScore()).isEqualTo(5);
    }

    @Test
    void reviewAiQuestion_rejected_setsStatusRejectedAndDoesNotCopy() {
        // Arrange
        mockCurrentUserWithRole("ADMIN");
        AiQuestion aiQuestion = mockPendingAiQuestion(questionType(1, "SINGLE_CHOICE"));
        List<AiQuestionAnswer> aiQuestionAnswers = mockAiQuestionAnswers();

        // Act
        aiQuestionBankService.reviewAiQuestion(AI_QUESTION_ID, review(false));

        // Assert
        assertThat(aiQuestion.getStatus()).isEqualTo("R");
        assertThat(aiQuestionAnswers).extracting("status").containsOnly("R");
        verify(questionRepository, never()).save(any());
        verify(questionAnswerRepository, never()).save(any());
    }

    @Test
    void reviewAiQuestion_alreadyReviewed_throwsBadRequestException() {
        // Arrange
        mockCurrentUserWithRole("ADMIN");
        AiQuestion aiQuestion = mockPendingAiQuestion(questionType(1, "SINGLE_CHOICE"));
        aiQuestion.setStatus("A");

        // Act + Assert
        assertThatThrownBy(() -> aiQuestionBankService.reviewAiQuestion(AI_QUESTION_ID, review(true)))
                .isInstanceOf(BadRequestException.class);

        verify(aiQuestionRepository, never()).save(any());
        verify(questionRepository, never()).save(any());
    }

    @Test
    void reviewAiQuestion_aiQuestionDoesNotExist_throwsPrimaryKeyNotFoundException() {
        // Arrange
        mockCurrentUserWithRole("ADMIN");
        when(aiQuestionRepository.findById(99)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> aiQuestionBankService.reviewAiQuestion(99, review(true)))
                .isInstanceOf(PrimaryKeyNotFoundException.class);
    }

    @Test
    void reviewAiQuestion_userWithoutPermission_throwsForbiddenAndChangesNothing() {
        // Arrange
        mockCurrentUserWithRole("KASUTAJA");

        // Act + Assert
        assertThatThrownBy(() -> aiQuestionBankService.reviewAiQuestion(AI_QUESTION_ID, review(true)))
                .isInstanceOf(ForbiddenException.class);

        verify(aiQuestionRepository, never()).save(any());
        verify(questionRepository, never()).save(any());
    }

    // --- Nimekiri ---

    @Test
    void findAllAiQuestionsBy_questionsExist_addsTypeNameAndAnswers() {
        // Arrange
        mockCurrentUserWithRole("ADMIN");
        AiQuestion aiQuestion = new AiQuestion();
        aiQuestion.setQuestionTypeId(2);
        when(aiQuestionRepository.findAiQuestionsBy("P", null)).thenReturn(List.of(aiQuestion));
        when(questionTypeRepository.findAll()).thenReturn(List.of(questionType(1, "SINGLE_CHOICE"), questionType(2, "MULTIPLE_CHOICE")));

        AiQuestionBankDto aiQuestionBankDto = new AiQuestionBankDto();
        aiQuestionBankDto.setAiQuestionId(AI_QUESTION_ID);
        when(aiQuestionMapper.toAiQuestionBankDto(aiQuestion)).thenReturn(aiQuestionBankDto);

        List<AiQuestionAnswer> aiQuestionAnswers = mockAiQuestionAnswers();
        List<AiQuestionBankAnswerDto> aiQuestionBankAnswerDtos = List.of(new AiQuestionBankAnswerDto());
        when(aiQuestionAnswerMapper.toAiQuestionBankAnswerDtos(aiQuestionAnswers)).thenReturn(aiQuestionBankAnswerDtos);

        // Act
        List<AiQuestionBankDto> aiQuestionBankDtos = aiQuestionBankService.findAllAiQuestionsBy("P", null);

        // Assert
        assertThat(aiQuestionBankDtos).hasSize(1);
        assertThat(aiQuestionBankDtos.get(0).getQuestionTypeName()).isEqualTo("MULTIPLE_CHOICE");
        assertThat(aiQuestionBankDtos.get(0).getAnswers()).isSameAs(aiQuestionBankAnswerDtos);
    }

    @Test
    void findAllAiQuestionsBy_userWithoutPermission_throwsForbiddenException() {
        // Arrange
        mockCurrentUserWithRole("KASUTAJA");

        // Act + Assert
        assertThatThrownBy(() -> aiQuestionBankService.findAllAiQuestionsBy("P", null))
                .isInstanceOf(ForbiddenException.class);

        verify(aiQuestionRepository, never()).findAiQuestionsBy(any(), any());
    }

}
