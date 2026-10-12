package ee.testiplatvorm.service;

import ee.testiplatvorm.controller.aiquestion.dto.AiChatMessageDto;
import ee.testiplatvorm.controller.aiquestion.dto.AiQuestionGenerationResponse;
import ee.testiplatvorm.controller.aiquestion.dto.AiQuestionSaveRequest;
import ee.testiplatvorm.controller.aiquestion.dto.AskRequest;
import ee.testiplatvorm.controller.aiquestion.dto.GeneratedAnswerDto;
import ee.testiplatvorm.controller.aiquestion.dto.GeneratedQuestionDto;
import ee.testiplatvorm.infrastructure.exception.BadRequestException;
import ee.testiplatvorm.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.testiplatvorm.persistence.questiontype.QuestionType;
import ee.testiplatvorm.persistence.questiontype.QuestionTypeRepository;
import ee.testiplatvorm.persistence.aiquestion.AiQuestion;
import ee.testiplatvorm.persistence.aiquestion.AiQuestionRepository;
import ee.testiplatvorm.persistence.aiquestionanswer.AiQuestionAnswer;
import ee.testiplatvorm.persistence.aiquestionanswer.AiQuestionAnswerRepository;
import ee.testiplatvorm.persistence.competencelevel.CompetenceLevel;
import ee.testiplatvorm.persistence.competencelevel.CompetenceLevelRepository;
import ee.testiplatvorm.persistence.question.Question;
import ee.testiplatvorm.persistence.question.QuestionRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static ee.testiplatvorm.Error.AI_QUESTION_GENERATION_FAILED;
import static ee.testiplatvorm.Error.INVALID_AI_QUESTION;
import static ee.testiplatvorm.Status.STATUS_ACTIVE;
import static ee.testiplatvorm.Status.STATUS_PENDING;
import static ee.testiplatvorm.service.AiQuestionValidationService.MAX_ANSWER_COUNT;

@Service
public class AiQuestionService {

    private static final int MAX_QUESTION_COUNT = 5;

    private static final String SELECTION_SYSTEM_PROMPT = """
            You help a question author on a competence testing platform.
            The author's free-text request may ask for questions for one or several competence levels.
            Split the request into targets: each target has a competence level, a question type
            and how many questions to create for it.

            RULES:
            1. Choose competenceLevelId ONLY from the AVAILABLE COMPETENCE LEVELS list.
            2. Choose questionTypeId ONLY from the AVAILABLE QUESTION TYPES list.
            3. The author may write in Estonian or English and may use synonyms
               (e.g. "algaja" = beginner, "JS" = JavaScript, "valikvastustega" = choice question).
            4. If the question type is not mentioned, choose SINGLE_CHOICE.
            5. questionCount of a target is 1 if the author did not mention a number.
            6. The total questionCount of all targets must never exceed %d.
               If the author asks for more, reduce the counts so that the total is %d.
            7. Use one target per distinct competence level + question type combination.
            8. If a competence or a level cannot be determined with confidence, or the request matches
               several competence levels equally, return an empty targets list and write a short
               clarifyingQuestion in Estonian that names the available options. If the author named a
               competence but the requested level does not exist for it, name the levels that do exist
               for that competence.
            9. If everything is clear, set clarifyingQuestion to null.

            CONVERSATION RULES:
            10. The request may contain EARLIER MESSAGES of the same conversation (author's messages and
                your own clarifying questions) and always contains the LATEST AUTHOR MESSAGE.
            11. The LATEST AUTHOR MESSAGE always has priority. If it names a different competence, level,
                question type or question count than the earlier messages, use the values from the latest
                message and ignore the conflicting earlier values. The author is allowed to change their mind.
            12. Use earlier messages only to fill in what the latest message does not say. For example, if the
                latest message only answers your clarifying question (e.g. "Juunior" or "Vue"), combine it with
                the competence, level, type, count and topic from the earlier messages.
            13. Never ask a clarifying question that the latest message already answers, and never repeat the
                same clarifying question - if something is still unclear, ask only about the part that is missing.
            """.formatted(MAX_QUESTION_COUNT, MAX_QUESTION_COUNT);

    private static final String SELECTION_USER_PROMPT_TEMPLATE = """
            AVAILABLE COMPETENCE LEVELS:
            %s

            AVAILABLE QUESTION TYPES:
            %s

            %s
            """;

    private static final String QUESTION_SYSTEM_PROMPT = """
            You are a test question generator for a competence testing platform.
            You write new test questions with their answer options.

            QUESTION TYPES:
            - SINGLE_CHOICE: 3-4 answers, exactly ONE answer has isCorrect = true.
            - MULTIPLE_CHOICE: 4-5 answers, at least ONE answer has isCorrect = true.
            - TRUE_FALSE: exactly 2 answers "Tõene" and "Väär", exactly ONE has isCorrect = true.

            RULES:
            1. Write the questions and answers in Estonian.
            2. The questions must match the given competence and level difficulty.
            3. Do not repeat or rephrase any of the existing questions, and do not repeat questions within this batch.
            4. title is the question itself, max 100 characters, without code.
            5. description is a short instruction for the test taker (e.g. "Vali õige vastus."), max 1000 characters.
            6. Each answerText is max 255 characters.
            7. A question must NEVER have more than %d answers in total.
            8. Wrong answers must be plausible, but clearly incorrect to an expert.
            9. Answer texts must NOT reveal which answer is correct or wrong:
               - never add labels like "(õige)", "(vale)", "correct", "wrong", ✓ or ✗
               - the correct answer must not be noticeably longer or more detailed than the wrong ones
               - do not use "Kõik eelnevad" or "Mitte ükski eelnevatest" as answers
            10. If a code example is needed, put it only in description, and it must be at most 10 lines long.
            """.formatted(MAX_ANSWER_COUNT);

    private static final String QUESTION_USER_PROMPT_TEMPLATE = """
            Generate exactly %d %s question(s).

            Competence: %s
            Competence description: %s
            Level: %s (%s)

            Existing questions for this competence level:
            %s

            Author's request (may also contain topic wishes and requests for other competences -
            follow only the parts that concern this competence and level; if earlier messages and the
            LATEST AUTHOR MESSAGE disagree, follow the latest message):
            %s
            """;

    private static final String DEFAULT_CLARIFYING_QUESTION = "Millise kompetentsi ja taseme jaoks küsimus luua?";

    // AI vastuste kujud - neid ei tagastata otse frontendile
    public record QuestionTarget(Integer competenceLevelId, Integer questionTypeId, Integer questionCount) {
    }

    public record QuestionTargets(List<QuestionTarget> targets, String clarifyingQuestion) {
    }

    // Valideeritud sihtrühm: andmebaasist leitud tase ja tüüp ning lõplik küsimuste arv
    private record ValidQuestionTarget(CompetenceLevel competenceLevel, QuestionType questionType, int questionCount) {
    }

    public record AiGeneratedQuestion(String title, String description, List<GeneratedAnswerDto> answers) {
    }

    public record AiGeneratedQuestions(List<AiGeneratedQuestion> questions) {
    }

    private final ChatClient chatClient;
    private final CompetenceLevelRepository competenceLevelRepository;
    private final QuestionTypeRepository questionTypeRepository;
    private final QuestionRepository questionRepository;
    private final AiQuestionRepository aiQuestionRepository;
    private final AiQuestionAnswerRepository aiQuestionAnswerRepository;
    private final CurrentUserService currentUserService;
    private final AiQuestionValidationService aiQuestionValidationService;

    public AiQuestionService(ChatClient.Builder builder,
                             CompetenceLevelRepository competenceLevelRepository,
                             QuestionTypeRepository questionTypeRepository,
                             QuestionRepository questionRepository,
                             AiQuestionRepository aiQuestionRepository,
                             AiQuestionAnswerRepository aiQuestionAnswerRepository,
                             CurrentUserService currentUserService,
                             AiQuestionValidationService aiQuestionValidationService) {
        this.chatClient = builder.build();
        this.competenceLevelRepository = competenceLevelRepository;
        this.questionTypeRepository = questionTypeRepository;
        this.questionRepository = questionRepository;
        this.aiQuestionRepository = aiQuestionRepository;
        this.aiQuestionAnswerRepository = aiQuestionAnswerRepository;
        this.currentUserService = currentUserService;
        this.aiQuestionValidationService = aiQuestionValidationService;
    }

    public AiQuestionGenerationResponse generateQuestions(AskRequest askRequest) {
        // Õigused kontrollitakse enne AI päringut, et ilma õiguseta päringutele raha ei kuluks
        currentUserService.getValidQuestionAuthorId();
        String instructions = createConversationText(askRequest);

        List<CompetenceLevel> competenceLevels = competenceLevelRepository.findAllCompetenceLevelsBy(STATUS_ACTIVE.getCode());
        List<QuestionType> questionTypes = questionTypeRepository.findAll();

        QuestionTargets questionTargets = selectQuestionTargets(instructions, competenceLevels, questionTypes);
        Optional<List<ValidQuestionTarget>> validQuestionTargets = getValidQuestionTargets(questionTargets, competenceLevels, questionTypes);
        if (validQuestionTargets.isEmpty()) {
            return createClarifyingResponse(questionTargets.clarifyingQuestion());
        }

        List<GeneratedQuestionDto> generatedQuestionDtos = new ArrayList<>();
        for (ValidQuestionTarget validQuestionTarget : validQuestionTargets.get()) {
            generatedQuestionDtos.addAll(generateQuestionDtosFor(validQuestionTarget, instructions));
        }

        if (generatedQuestionDtos.isEmpty()) {
            throw new BadRequestException(AI_QUESTION_GENERATION_FAILED.getMessage(), AI_QUESTION_GENERATION_FAILED.name());
        }

        AiQuestionGenerationResponse aiQuestionGenerationResponse = new AiQuestionGenerationResponse();
        aiQuestionGenerationResponse.setQuestions(generatedQuestionDtos);
        return aiQuestionGenerationResponse;
    }

    @Transactional
    public Integer saveQuestion(AiQuestionSaveRequest aiQuestionSaveRequest) {
        Integer userId = currentUserService.getValidQuestionAuthorId();

        Integer competenceLevelId = aiQuestionSaveRequest.getCompetenceLevelId();
        CompetenceLevel competenceLevel = competenceLevelRepository.findById(competenceLevelId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("competenceLevelId", competenceLevelId));

        Integer questionTypeId = aiQuestionSaveRequest.getQuestionTypeId();
        QuestionType questionType = questionTypeRepository.findById(questionTypeId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("questionTypeId", questionTypeId));

        boolean isValidQuestion = aiQuestionValidationService.isValidQuestion(aiQuestionSaveRequest.getTitle(), aiQuestionSaveRequest.getDescription(),
                aiQuestionSaveRequest.getAnswers(), questionType);
        if (!isValidQuestion) {
            throw new BadRequestException(INVALID_AI_QUESTION.getMessage(), INVALID_AI_QUESTION.name());
        }

        AiQuestion aiQuestion = saveAiQuestion(aiQuestionSaveRequest, competenceLevel, questionTypeId, userId);
        saveAiQuestionAnswers(aiQuestionSaveRequest.getAnswers(), aiQuestion);
        return aiQuestion.getId();
    }

    // Varasemad sõnumid + viimane sõnum ühe tekstina, viimane sõnum on eraldi esile tõstetud
    private String createConversationText(AskRequest askRequest) {
        String latestMessage = "LATEST AUTHOR MESSAGE (has priority):\n" + askRequest.getInstructions();
        List<AiChatMessageDto> previousMessages = askRequest.getPreviousMessages();
        if (previousMessages == null || previousMessages.isEmpty()) {
            return latestMessage;
        }

        String earlierMessages = String.join("\n", previousMessages.stream()
                .map(message -> ("ai".equals(message.getSender()) ? "Assistant: " : "Author: ") + message.getText())
                .toList());
        return "EARLIER MESSAGES (oldest first):\n" + earlierMessages + "\n\n" + latestMessage;
    }

    private QuestionTargets selectQuestionTargets(String instructions, List<CompetenceLevel> competenceLevels, List<QuestionType> questionTypes) {
        String competenceLevelList = String.join("\n", competenceLevels.stream()
                .map(competenceLevel -> "- id=" + competenceLevel.getId()
                        + ": " + competenceLevel.getCompetence().getName()
                        + " / " + competenceLevel.getLevel().getName())
                .toList());
        String questionTypeList = String.join("\n", questionTypes.stream()
                .map(questionType -> "- id=" + questionType.getId() + ": " + questionType.getName())
                .toList());

        String userPrompt = SELECTION_USER_PROMPT_TEMPLATE.formatted(competenceLevelList, questionTypeList, instructions);
        return callLlm(SELECTION_SYSTEM_PROMPT, userPrompt, QuestionTargets.class);
    }

    // Tühi tulemus tähendab, et tuleb küsida täpsustust (AI küsis ise või valis olematu taseme/tüübi)
    private Optional<List<ValidQuestionTarget>> getValidQuestionTargets(QuestionTargets questionTargets,
                                                                        List<CompetenceLevel> competenceLevels,
                                                                        List<QuestionType> questionTypes) {
        boolean hasClarifyingQuestion = questionTargets.clarifyingQuestion() != null && !questionTargets.clarifyingQuestion().isBlank();
        if (hasClarifyingQuestion || questionTargets.targets() == null || questionTargets.targets().isEmpty()) {
            return Optional.empty();
        }

        List<ValidQuestionTarget> validQuestionTargets = new ArrayList<>();
        int remainingQuestionCount = MAX_QUESTION_COUNT;
        for (QuestionTarget questionTarget : questionTargets.targets()) {
            Optional<CompetenceLevel> competenceLevel = competenceLevels.stream()
                    .filter(level -> level.getId().equals(questionTarget.competenceLevelId()))
                    .findFirst();
            Optional<QuestionType> questionType = questionTypes.stream()
                    .filter(type -> type.getId().equals(questionTarget.questionTypeId()))
                    .findFirst();
            if (competenceLevel.isEmpty() || questionType.isEmpty()) {
                return Optional.empty();
            }

            // Kokku mitte üle 5 küsimuse, isegi kui AI eiras reeglit
            int questionCount = Math.min(getValidQuestionCount(questionTarget.questionCount()), remainingQuestionCount);
            if (questionCount > 0) {
                validQuestionTargets.add(new ValidQuestionTarget(competenceLevel.get(), questionType.get(), questionCount));
                remainingQuestionCount -= questionCount;
            }
        }
        return Optional.of(validQuestionTargets);
    }

    private int getValidQuestionCount(Integer questionCount) {
        if (questionCount == null || questionCount < 1) {
            return 1;
        }
        return Math.min(questionCount, MAX_QUESTION_COUNT);
    }

    private List<GeneratedQuestionDto> generateQuestionDtosFor(ValidQuestionTarget validQuestionTarget, String instructions) {
        CompetenceLevel competenceLevel = validQuestionTarget.competenceLevel();
        QuestionType questionType = validQuestionTarget.questionType();
        int questionCount = validQuestionTarget.questionCount();

        List<AiGeneratedQuestion> aiGeneratedQuestions = generateQuestionsFor(competenceLevel, questionType, questionCount, instructions);
        return createQuestionDtos(aiGeneratedQuestions, questionCount, competenceLevel, questionType);
    }

    private AiQuestionGenerationResponse createClarifyingResponse(String clarifyingQuestion) {
        AiQuestionGenerationResponse aiQuestionGenerationResponse = new AiQuestionGenerationResponse();
        boolean hasClarifyingQuestion = clarifyingQuestion != null && !clarifyingQuestion.isBlank();
        aiQuestionGenerationResponse.setClarifyingQuestion(hasClarifyingQuestion ? clarifyingQuestion : DEFAULT_CLARIFYING_QUESTION);
        return aiQuestionGenerationResponse;
    }

    private List<AiGeneratedQuestion> generateQuestionsFor(CompetenceLevel competenceLevel, QuestionType questionType,
                                                           int questionCount, String instructions) {
        List<Question> existingQuestions = questionRepository.findQuestionsBy(competenceLevel.getId(), STATUS_ACTIVE.getCode());
        String existingQuestionTitles = existingQuestions.isEmpty()
                ? "(none)"
                : String.join("\n", existingQuestions.stream().map(question -> "- " + question.getTitle()).toList());

        String userPrompt = QUESTION_USER_PROMPT_TEMPLATE.formatted(
                questionCount,
                questionType.getName(),
                competenceLevel.getCompetence().getName(),
                competenceLevel.getCompetence().getDescription(),
                competenceLevel.getLevel().getName(),
                competenceLevel.getLevel().getDescription(),
                existingQuestionTitles,
                instructions);

        AiGeneratedQuestions aiGeneratedQuestions = callLlm(QUESTION_SYSTEM_PROMPT, userPrompt, AiGeneratedQuestions.class);
        return aiGeneratedQuestions.questions() == null ? List.of() : aiGeneratedQuestions.questions();
    }

    // Reeglitele mittevastavad küsimused jäetakse välja, ülejäänud näidatakse kasutajale
    private List<GeneratedQuestionDto> createQuestionDtos(List<AiGeneratedQuestion> aiGeneratedQuestions, int questionCount,
                                                          CompetenceLevel competenceLevel, QuestionType questionType) {
        List<GeneratedQuestionDto> generatedQuestionDtos = new ArrayList<>();
        for (AiGeneratedQuestion aiGeneratedQuestion : aiGeneratedQuestions) {
            if (generatedQuestionDtos.size() == questionCount) {
                break;
            }
            if (aiQuestionValidationService.isValidQuestion(aiGeneratedQuestion.title(), aiGeneratedQuestion.description(), aiGeneratedQuestion.answers(), questionType)) {
                generatedQuestionDtos.add(createQuestionDto(aiGeneratedQuestion, competenceLevel, questionType));
            }
        }
        return generatedQuestionDtos;
    }

    private GeneratedQuestionDto createQuestionDto(AiGeneratedQuestion aiGeneratedQuestion,
                                                   CompetenceLevel competenceLevel, QuestionType questionType) {
        List<GeneratedAnswerDto> answers = new ArrayList<>(aiGeneratedQuestion.answers());
        // AI paneb õige vastuse sageli esimeseks - segamine ei lase asukohal vastust reeta
        // TRUE_FALSE puhul jääb järjekord "Tõene", "Väär"
        if (!questionType.getName().equals("TRUE_FALSE")) {
            Collections.shuffle(answers);
        }

        GeneratedQuestionDto generatedQuestionDto = new GeneratedQuestionDto();
        generatedQuestionDto.setTitle(truncate(aiGeneratedQuestion.title(), 100));
        generatedQuestionDto.setDescription(truncate(aiGeneratedQuestion.description(), 1000));
        generatedQuestionDto.setAnswers(answers);
        generatedQuestionDto.setCompetenceLevelId(competenceLevel.getId());
        generatedQuestionDto.setCompetenceName(competenceLevel.getCompetence().getName());
        generatedQuestionDto.setLevelName(competenceLevel.getLevel().getName());
        generatedQuestionDto.setQuestionTypeId(questionType.getId());
        generatedQuestionDto.setQuestionTypeName(questionType.getName());
        return generatedQuestionDto;
    }

    private AiQuestion saveAiQuestion(AiQuestionSaveRequest aiQuestionSaveRequest, CompetenceLevel competenceLevel, Integer questionTypeId, Integer userId) {
        AiQuestion aiQuestion = new AiQuestion();
        aiQuestion.setTitle(truncate(aiQuestionSaveRequest.getTitle(), 100));
        aiQuestion.setDescription(truncate(aiQuestionSaveRequest.getDescription(), 1000));
        aiQuestion.setCompetence(competenceLevel.getCompetence());
        aiQuestion.setCompetenceLevel(competenceLevel);
        aiQuestion.setQuestionTypeId(questionTypeId);
        aiQuestion.setStatus(STATUS_PENDING.getCode());
        aiQuestion.setCreatedBy(userId);
        OffsetDateTime currentTime = OffsetDateTime.now();
        aiQuestion.setCreatedAt(currentTime);
        aiQuestion.setUpdatedAt(currentTime);
        return aiQuestionRepository.save(aiQuestion);
    }

    private void saveAiQuestionAnswers(List<GeneratedAnswerDto> generatedAnswerDtos, AiQuestion aiQuestion) {
        for (GeneratedAnswerDto generatedAnswerDto : generatedAnswerDtos) {
            AiQuestionAnswer aiQuestionAnswer = new AiQuestionAnswer();
            aiQuestionAnswer.setAiQuestion(aiQuestion);
            aiQuestionAnswer.setAnswerText(truncate(generatedAnswerDto.getAnswerText(), 255));
            aiQuestionAnswer.setIsCorrect(Boolean.TRUE.equals(generatedAnswerDto.getIsCorrect()));
            aiQuestionAnswer.setStatus(STATUS_PENDING.getCode());
            // score ja feedback on andmebaasis NOT NULL, aga hinnatakse alles ülevaatamisel
            aiQuestionAnswer.setScore(0);
            aiQuestionAnswer.setFeedback(0);
            aiQuestionAnswerRepository.save(aiQuestionAnswer);
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }

    private <T> T callLlm(String systemPrompt, String userPrompt, Class<T> responseType) {
        T response = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .responseEntity(responseType)
                .getEntity();

        if (response == null) {
            throw new IllegalStateException("AI model returned an empty response");
        }

        return response;
    }
}
