package ee.testiplatvorm.service;

import ee.testiplatvorm.controller.aiquestion.dto.AiQuestionBankAnswerDto;
import ee.testiplatvorm.controller.aiquestion.dto.AiQuestionBankDto;
import ee.testiplatvorm.controller.aiquestion.dto.AiQuestionReviewRequestDto;
import ee.testiplatvorm.infrastructure.exception.BadRequestException;
import ee.testiplatvorm.infrastructure.exception.ForbiddenException;
import ee.testiplatvorm.infrastructure.exception.PrimaryKeyNotFoundException;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static ee.testiplatvorm.Error.AI_QUESTION_ALREADY_REVIEWED;
import static ee.testiplatvorm.Error.NO_PERMISSION_TO_MANAGE_QUESTIONS;
import static ee.testiplatvorm.Status.STATUS_ACTIVE;
import static ee.testiplatvorm.Status.STATUS_PENDING;
import static ee.testiplatvorm.Status.STATUS_REJECTED;

/**
 * AI küsimuste ülevaatamine küsimuste pangas (nimekiri, hiljem ka kinnitamine ja tagasilükkamine).
 * AI-ga genereerimine ja salvestamine on AiQuestionService-s.
 */
@Service
@RequiredArgsConstructor
public class AiQuestionBankService {

    // Küsimuste panka kopeeritud küsimuse punktid (samad, mis testandmetes)
    private static final int TRUE_FALSE_QUESTION_SCORE = 5;
    private static final int DEFAULT_QUESTION_SCORE = 10;
    private static final String TRUE_FALSE = "TRUE_FALSE";

    private final AiQuestionRepository aiQuestionRepository;
    private final AiQuestionMapper aiQuestionMapper;
    private final AiQuestionAnswerRepository aiQuestionAnswerRepository;
    private final AiQuestionAnswerMapper aiQuestionAnswerMapper;
    private final QuestionTypeRepository questionTypeRepository;
    private final QuestionRepository questionRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final UserService userService;
    private final CurrentUserService currentUserService;

    public List<AiQuestionBankDto> findAllAiQuestionsBy(String status, Integer competenceId) {
        validateUserCanManageQuestions();
        List<AiQuestion> aiQuestions = aiQuestionRepository.findAiQuestionsBy(status, competenceId);
        List<QuestionType> questionTypes = questionTypeRepository.findAll();
        List<AiQuestionBankDto> aiQuestionBankDtos = new ArrayList<>();

        for (AiQuestion aiQuestion : aiQuestions) {
            AiQuestionBankDto aiQuestionBankDto = aiQuestionMapper.toAiQuestionBankDto(aiQuestion);
            handleAddQuestionTypeName(aiQuestionBankDto, questionTypes, aiQuestion.getQuestionTypeId());
            handleAddAnswers(aiQuestionBankDto);
            aiQuestionBankDtos.add(aiQuestionBankDto);
        }
        return aiQuestionBankDtos;
    }

    @Transactional
    public void reviewAiQuestion(Integer aiQuestionId, AiQuestionReviewRequestDto aiQuestionReviewRequestDto) {
        validateUserCanManageQuestions();
        AiQuestion aiQuestion = getValidAiQuestionBy(aiQuestionId);
        if (!STATUS_PENDING.getCode().equals(aiQuestion.getStatus())) {
            throw new BadRequestException(AI_QUESTION_ALREADY_REVIEWED.getMessage(), AI_QUESTION_ALREADY_REVIEWED.name());
        }

        boolean isApproved = aiQuestionReviewRequestDto.getApproved();
        String reviewStatus = isApproved ? STATUS_ACTIVE.getCode() : STATUS_REJECTED.getCode();
        List<AiQuestionAnswer> aiQuestionAnswers = aiQuestionAnswerRepository.findAnswersBy(aiQuestionId);

        updateAiQuestionReview(aiQuestion, aiQuestionReviewRequestDto, reviewStatus);
        updateAiQuestionAnswerStatuses(aiQuestionAnswers, reviewStatus);
        if (isApproved) {
            copyToQuestionBank(aiQuestion, aiQuestionAnswers);
        }
    }

    private void validateUserCanManageQuestions() {
        User user = userService.getValidUserBy(currentUserService.getUserId());
        String roleName = user.getRole().getName();

        boolean isAllowedToManageQuestions = roleName.equals("ADMIN") || roleName.equals("HALDUR");
        if (!isAllowedToManageQuestions) {
            throw new ForbiddenException(NO_PERMISSION_TO_MANAGE_QUESTIONS.getMessage(), NO_PERMISSION_TO_MANAGE_QUESTIONS.name());
        }
    }

    public AiQuestion getValidAiQuestionBy(Integer aiQuestionId) {
        return aiQuestionRepository.findById(aiQuestionId).orElseThrow(() -> new PrimaryKeyNotFoundException("aiQuestionId", aiQuestionId));
    }

    private void updateAiQuestionReview(AiQuestion aiQuestion, AiQuestionReviewRequestDto aiQuestionReviewRequestDto, String reviewStatus) {
        aiQuestion.setScore(aiQuestionReviewRequestDto.getScore());
        aiQuestion.setFeedback(aiQuestionReviewRequestDto.getFeedback());
        aiQuestion.setIsGood(aiQuestionReviewRequestDto.getApproved());
        aiQuestion.setStatus(reviewStatus);
        aiQuestion.setUpdatedAt(OffsetDateTime.now());
        aiQuestionRepository.save(aiQuestion);
    }

    private void updateAiQuestionAnswerStatuses(List<AiQuestionAnswer> aiQuestionAnswers, String reviewStatus) {
        for (AiQuestionAnswer aiQuestionAnswer : aiQuestionAnswers) {
            aiQuestionAnswer.setStatus(reviewStatus);
            aiQuestionAnswerRepository.save(aiQuestionAnswer);
        }
    }

    // Kinnitatud AI küsimus kopeeritakse koos vastustega question ja question_answer tabelitesse
    private void copyToQuestionBank(AiQuestion aiQuestion, List<AiQuestionAnswer> aiQuestionAnswers) {
        Question question = createQuestion(aiQuestion);
        questionRepository.save(question);
        for (AiQuestionAnswer aiQuestionAnswer : aiQuestionAnswers) {
            QuestionAnswer questionAnswer = createQuestionAnswer(aiQuestionAnswer, question);
            questionAnswerRepository.save(questionAnswer);
        }
    }

    private Question createQuestion(AiQuestion aiQuestion) {
        Integer questionTypeId = aiQuestion.getQuestionTypeId();
        QuestionType questionType = questionTypeRepository.findById(questionTypeId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("questionTypeId", questionTypeId));

        Question question = new Question();
        question.setCompetence(aiQuestion.getCompetence());
        question.setCompetenceLevel(aiQuestion.getCompetenceLevel());
        question.setTitle(aiQuestion.getTitle());
        question.setDescription(aiQuestion.getDescription());
        question.setQuestionType(questionType);
        question.setScore(getQuestionScoreBy(questionType));
        question.setStatus(STATUS_ACTIVE.getCode());
        question.setCreatedBy(userService.getValidUserBy(aiQuestion.getCreatedBy()));
        OffsetDateTime currentTime = OffsetDateTime.now();
        question.setCreatedAt(currentTime);
        question.setUpdatedAt(currentTime);
        return question;
    }

    private int getQuestionScoreBy(QuestionType questionType) {
        return TRUE_FALSE.equals(questionType.getName()) ? TRUE_FALSE_QUESTION_SCORE : DEFAULT_QUESTION_SCORE;
    }

    private QuestionAnswer createQuestionAnswer(AiQuestionAnswer aiQuestionAnswer, Question question) {
        QuestionAnswer questionAnswer = new QuestionAnswer();
        questionAnswer.setQuestion(question);
        questionAnswer.setAnswerText(aiQuestionAnswer.getAnswerText());
        questionAnswer.setCorrectChoice(aiQuestionAnswer.getIsCorrect());
        questionAnswer.setStatus(STATUS_ACTIVE.getCode());
        return questionAnswer;
    }

    // ai_question tabelis on question_type_id ilma seoseta, seega tüübi nimi otsitakse eraldi
    private void handleAddQuestionTypeName(AiQuestionBankDto aiQuestionBankDto, List<QuestionType> questionTypes, Integer questionTypeId) {
        for (QuestionType questionType : questionTypes) {
            if (questionType.getId().equals(questionTypeId)) {
                aiQuestionBankDto.setQuestionTypeName(questionType.getName());
            }
        }
    }

    private void handleAddAnswers(AiQuestionBankDto aiQuestionBankDto) {
        List<AiQuestionAnswer> aiQuestionAnswers = aiQuestionAnswerRepository.findAnswersBy(aiQuestionBankDto.getAiQuestionId());
        List<AiQuestionBankAnswerDto> aiQuestionBankAnswerDtos = aiQuestionAnswerMapper.toAiQuestionBankAnswerDtos(aiQuestionAnswers);
        aiQuestionBankDto.setAnswers(aiQuestionBankAnswerDtos);
    }
}
