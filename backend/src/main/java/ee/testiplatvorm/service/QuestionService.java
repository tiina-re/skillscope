package ee.testiplatvorm.service;

import ee.testiplatvorm.Error;
import ee.testiplatvorm.controller.question.dto.*;
import ee.testiplatvorm.infrastructure.exception.BadRequestException;
import ee.testiplatvorm.infrastructure.exception.ForbiddenException;
import ee.testiplatvorm.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.testiplatvorm.persistence.competencelevel.CompetenceLevel;
import ee.testiplatvorm.persistence.competencelevel.CompetenceLevelRepository;
import ee.testiplatvorm.persistence.question.Question;
import ee.testiplatvorm.persistence.question.QuestionMapper;
import ee.testiplatvorm.persistence.question.QuestionRepository;
import ee.testiplatvorm.persistence.questionanswer.QuestionAnswer;
import ee.testiplatvorm.persistence.questionanswer.QuestionAnswerMapper;
import ee.testiplatvorm.persistence.questionanswer.QuestionAnswerRepository;
import ee.testiplatvorm.persistence.questiontype.QuestionType;
import ee.testiplatvorm.persistence.questiontype.QuestionTypeRepository;
import ee.testiplatvorm.persistence.user.User;
import ee.testiplatvorm.persistence.user.UserMapper;
import ee.testiplatvorm.persistence.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

import static ee.testiplatvorm.Error.NO_PERMISSION_TO_CREATE_QUESTIONS;
import static ee.testiplatvorm.Error.NO_PERMISSION_TO_MANAGE_QUESTIONS;
import static ee.testiplatvorm.Status.STATUS_ACTIVE;
import static ee.testiplatvorm.Status.STATUS_INACTIVE;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final QuestionAnswerMapper questionAnswerMapper;
    private final CurrentUserService currentUserService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final QuestionTypeRepository questionTypeRepository;
    private final CompetenceLevelRepository competenceLevelRepository;
    private final UserService userService;

    public List<QuestionResponseDto> findQuestionsBy(Integer competenceLevelId) {
        validateUserCanManageQuestions();
        List<Question> questions = questionRepository.findQuestionsBy(competenceLevelId, STATUS_ACTIVE.getCode());
        List<QuestionResponseDto> questionResponseDtos = questionMapper.toQuestionResponseDtos(questions);
        return questionResponseDtos;
    }

    public List<QuestionBankDto> findAllQuestionsBy(Integer competenceId) {
        validateUserCanManageQuestions();
        List<Question> allQuestions = questionRepository.findAllQuestionsBy(competenceId);
        List<QuestionBankDto> questionBankDtos = questionMapper.toQuestionBankDtos(allQuestions);

        for (QuestionBankDto questionBankDto : questionBankDtos) {
            handleGetQuestionsWithAnswers(questionBankDto);
        }
        return questionBankDtos;
    }

    public void updateQuestion(Integer questionId, QuestionUpdateRequestDto questionUpdateRequestDto) {
        validateUserCanManageQuestions();
        Question question = getValidQuestionBy(questionId);
        questionMapper.updateQuestion(questionUpdateRequestDto, question);
        question.setUpdatedAt(OffsetDateTime.now());
        questionRepository.save(question);
    }

    public void deleteQuestion(Integer questionId) {
        validateUserCanManageQuestions();
        Question question = getValidQuestionBy(questionId);
        question.setStatus(STATUS_INACTIVE.getCode());
        question.setUpdatedAt(OffsetDateTime.now());
        questionRepository.save(question);
    }

    private void validateUserCanManageQuestions() {
        User user = userService.getValidUserBy(currentUserService.getUserId());
        String roleName = user.getRole().getName();

        boolean isAllowedToManageQuestions = roleName.equals("ADMIN") || roleName.equals("HALDUR");
        if (!isAllowedToManageQuestions) {
            throw new ForbiddenException(NO_PERMISSION_TO_MANAGE_QUESTIONS.getMessage(), NO_PERMISSION_TO_MANAGE_QUESTIONS.name());
        }
    }

    public Question getValidQuestionBy(Integer questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> new PrimaryKeyNotFoundException("questionId", questionId));
    }

    private void handleGetQuestionsWithAnswers(QuestionBankDto questionBankDto) {
        List<QuestionAnswer> questionAnswers = questionAnswerRepository.findAnswersBy(questionBankDto.getQuestionId(), STATUS_ACTIVE.getCode());
        List<QuestionBankAnswerDto> questionBankAnswerDtos = questionAnswerMapper.toQuestionBankAnswerDtos(questionAnswers);
        questionBankDto.setAnswers(questionBankAnswerDtos);
    }


    @Transactional
    public Integer createQuestion(QuestionCreateRequestDto questionCreateRequestDto) {
        Integer userId = currentUserService.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> (new PrimaryKeyNotFoundException("userId", userId)));
        String userRole = user.getRole().getName();

        boolean isAllowedToCreateQuestion = userRole.equals("ADMIN") || userRole.equals("HALDUR");
        if (!isAllowedToCreateQuestion) {
            throw new ForbiddenException(NO_PERMISSION_TO_CREATE_QUESTIONS.getMessage(), NO_PERMISSION_TO_CREATE_QUESTIONS.name());
        }

        Integer questionTypeId = questionCreateRequestDto.getQuestionTypeId();
        QuestionType questionType = questionTypeRepository.findById(questionTypeId).orElseThrow(() -> new PrimaryKeyNotFoundException("questionTypeId", questionTypeId));

        Integer competenceLevelId = questionCreateRequestDto.getCompetenceLevelId();
        CompetenceLevel competenceLevel = competenceLevelRepository.findById(competenceLevelId).orElseThrow(() -> new PrimaryKeyNotFoundException("competenceLevelId", competenceLevelId));

        int isCorrectCount = 0;
        for (QuestionCreateAnswerRequestDto questionCreateAnswerRequestDto : questionCreateRequestDto.getAnswers()) {
            if (questionCreateAnswerRequestDto.getIsCorrect()) {
                isCorrectCount++;
            }
        }

        String questionTypeName = questionType.getName();
        int answerCount = questionCreateRequestDto.getAnswers().size();

        boolean isValidCorrectAnswerCount = false;
        {
            if (questionTypeName.equals("SINGLE_CHOICE")) {
                isValidCorrectAnswerCount = isCorrectCount == 1;
            } else if (questionTypeName.equals("MULTIPLE_CHOICE")) {
                isValidCorrectAnswerCount = isCorrectCount >= 1;
            } else if (questionTypeName.equals("TRUE_FALSE")) {
                isValidCorrectAnswerCount = answerCount == 2 && isCorrectCount == 1;
            }
            if (!isValidCorrectAnswerCount) {
                throw new BadRequestException(Error.INVALID_CORRECT_ANSWER_COUNT.getMessage(), Error.INVALID_CORRECT_ANSWER_COUNT.name());
            }
        }

        Question question = questionMapper.toQuestion(questionCreateRequestDto);
        question.setCompetence(competenceLevel.getCompetence());
        question.setCompetenceLevel(competenceLevel);
        question.setQuestionType(questionType);
        question.setStatus(STATUS_ACTIVE.getCode());
        question.setCreatedBy(user);
        OffsetDateTime currentTime = OffsetDateTime.now();
        question.setCreatedAt(currentTime);
        question.setUpdatedAt(currentTime);
        questionRepository.save(question);

        List<QuestionAnswer> questionAnswers = questionAnswerMapper.toCreateQuestionAnswers(questionCreateRequestDto.getAnswers());
        for (QuestionAnswer questionAnswer : questionAnswers) {
            questionAnswer.setQuestion(question);
            questionAnswer.setStatus(STATUS_ACTIVE.getCode());
        }
        questionAnswerRepository.saveAll(questionAnswers);

        return question.getId();
    }
}
