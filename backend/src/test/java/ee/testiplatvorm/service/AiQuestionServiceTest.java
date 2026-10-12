package ee.testiplatvorm.service;

import ee.testiplatvorm.controller.aiquestion.dto.AiQuestionGenerationResponse;
import ee.testiplatvorm.controller.aiquestion.dto.AiQuestionSaveRequest;
import ee.testiplatvorm.controller.aiquestion.dto.AskRequest;
import ee.testiplatvorm.controller.aiquestion.dto.GeneratedAnswerDto;
import ee.testiplatvorm.infrastructure.exception.BadRequestException;
import ee.testiplatvorm.infrastructure.exception.ForbiddenException;
import ee.testiplatvorm.persistence.Level;
import ee.testiplatvorm.persistence.Role;
import ee.testiplatvorm.persistence.aiquestion.AiQuestion;
import ee.testiplatvorm.persistence.aiquestion.AiQuestionRepository;
import ee.testiplatvorm.persistence.aiquestionanswer.AiQuestionAnswer;
import ee.testiplatvorm.persistence.aiquestionanswer.AiQuestionAnswerRepository;
import ee.testiplatvorm.persistence.competence.Competence;
import ee.testiplatvorm.persistence.competencelevel.CompetenceLevel;
import ee.testiplatvorm.persistence.competencelevel.CompetenceLevelRepository;
import ee.testiplatvorm.persistence.question.QuestionRepository;
import ee.testiplatvorm.persistence.questiontype.QuestionType;
import ee.testiplatvorm.persistence.questiontype.QuestionTypeRepository;
import ee.testiplatvorm.persistence.user.User;
import ee.testiplatvorm.persistence.user.UserRepository;
import ee.testiplatvorm.service.AiQuestionService.AiGeneratedQuestion;
import ee.testiplatvorm.service.AiQuestionService.AiGeneratedQuestions;
import ee.testiplatvorm.service.AiQuestionService.QuestionTarget;
import ee.testiplatvorm.service.AiQuestionService.QuestionTargets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiQuestionServiceTest {

    private static final int USER_ID = 5;
    private static final int COMPETENCE_LEVEL_ID = 3;

    // Gemini asemel mock. RETURNS_DEEP_STUBS lubab stub'ida terve ahela
    // chatClient.prompt().system(...).user(...).call().responseEntity(...).getEntity() korraga.
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;
    @Mock
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private CompetenceLevelRepository competenceLevelRepository;
    @Mock
    private QuestionTypeRepository questionTypeRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private AiQuestionRepository aiQuestionRepository;
    @Mock
    private AiQuestionAnswerRepository aiQuestionAnswerRepository;
    @Mock
    private UserRepository userRepository;
    // Päris CurrentUserService, et rollikontroll (getValidQuestionAuthorId) oleks testitud; stub'itud on ainult sessioon
    private CurrentUserService currentUserService;

    private AiQuestionService aiQuestionService;

    private final QuestionType singleChoice = questionType(1, "SINGLE_CHOICE");
    private final QuestionType multipleChoice = questionType(2, "MULTIPLE_CHOICE");
    private final QuestionType trueFalse = questionType(3, "TRUE_FALSE");
    private CompetenceLevel competenceLevel;

    // AiQuestionService kutsub konstruktoris builder.build() – seepärast ei saa @InjectMocks-i kasutada,
    // teenus luuakse käsitsi pärast seda, kui builder on seadistatud.
    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        currentUserService = spy(new CurrentUserService(null, userRepository));
        aiQuestionService = new AiQuestionService(chatClientBuilder, competenceLevelRepository, questionTypeRepository,
                questionRepository, aiQuestionRepository, aiQuestionAnswerRepository, currentUserService,
                new AiQuestionValidationService());

        Competence competence = new Competence();
        competence.setName("Java");
        Level level = new Level();
        level.setName("Algaja");
        competenceLevel = new CompetenceLevel();
        competenceLevel.setId(COMPETENCE_LEVEL_ID);
        competenceLevel.setCompetence(competence);
        competenceLevel.setLevel(level);
    }

    // --- Abimeetodid ---

    private static QuestionType questionType(int questionTypeId, String name) {
        QuestionType questionType = new QuestionType();
        questionType.setId(questionTypeId);
        questionType.setName(name);
        return questionType;
    }

    private void mockCurrentUserWithRole(String roleName) {
        Role role = new Role();
        role.setName(roleName);
        User user = new User();
        user.setRole(role);
        doReturn(USER_ID).when(currentUserService).getUserId();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    }

    // Andmebaasis olevad tasemed ja tüübid, mille seast AI valib
    private void mockAvailableLevelsAndTypes() {
        when(competenceLevelRepository.findAllCompetenceLevelsBy("A")).thenReturn(List.of(competenceLevel));
        when(questionTypeRepository.findAll()).thenReturn(List.of(singleChoice, multipleChoice, trueFalse));
    }

    // "AI vastab" esimesele päringule: mis tasemele, mis tüüpi ja mitu küsimust luua
    private void mockAiSelects(QuestionTargets questionTargets) {
        when(chatClient.prompt().system(anyString()).user(anyString()).call()
                .responseEntity(QuestionTargets.class).getEntity())
                .thenReturn(questionTargets);
    }

    // "AI vastab" teisele päringule: genereeritud küsimused
    private void mockAiGenerates(AiGeneratedQuestion... aiGeneratedQuestions) {
        when(chatClient.prompt().system(anyString()).user(anyString()).call()
                .responseEntity(AiGeneratedQuestions.class).getEntity())
                .thenReturn(new AiGeneratedQuestions(List.of(aiGeneratedQuestions)));
    }

    private void mockAiSelectsAndGenerates(QuestionType questionType, int questionCount, AiGeneratedQuestion... aiGeneratedQuestions) {
        mockAiSelects(new QuestionTargets(List.of(new QuestionTarget(COMPETENCE_LEVEL_ID, questionType.getId(), questionCount)), null));
        mockAiGenerates(aiGeneratedQuestions);
    }

    private static GeneratedAnswerDto answer(String answerText, boolean isCorrect) {
        GeneratedAnswerDto generatedAnswerDto = new GeneratedAnswerDto();
        generatedAnswerDto.setAnswerText(answerText);
        generatedAnswerDto.setIsCorrect(isCorrect);
        return generatedAnswerDto;
    }

    // Korrektne SINGLE_CHOICE küsimus: 3 vastust, 1 õige
    private static AiGeneratedQuestion validQuestion(String title) {
        return new AiGeneratedQuestion(title, "Vali õige vastus.",
                List.of(answer("Vastus A", true), answer("Vastus B", false), answer("Vastus C", false)));
    }

    private static AiGeneratedQuestion question(String title, String description, GeneratedAnswerDto... answers) {
        return new AiGeneratedQuestion(title, description, List.of(answers));
    }

    private static AskRequest askRequest(String instructions) {
        AskRequest askRequest = new AskRequest();
        askRequest.setInstructions(instructions);
        return askRequest;
    }

    private static AiQuestionSaveRequest saveRequest(QuestionType questionType, GeneratedAnswerDto... answers) {
        AiQuestionSaveRequest aiQuestionSaveRequest = new AiQuestionSaveRequest();
        aiQuestionSaveRequest.setCompetenceLevelId(COMPETENCE_LEVEL_ID);
        aiQuestionSaveRequest.setQuestionTypeId(questionType.getId());
        aiQuestionSaveRequest.setTitle("Mis on JVM?");
        aiQuestionSaveRequest.setDescription("Vali õige vastus.");
        aiQuestionSaveRequest.setAnswers(List.of(answers));
        return aiQuestionSaveRequest;
    }

    // --- Õigused ja AI kulu ---

    @Test
    void generateQuestions_userWithoutPermission_throwsForbiddenAndDoesNotCallAi() {
        mockCurrentUserWithRole("KASUTAJA");

        assertThatThrownBy(() -> aiQuestionService.generateQuestions(askRequest("Loo 2 Java küsimust")))
                .isInstanceOf(ForbiddenException.class);

        // Ilma õiguseta päringule ei tohi AI raha kuluda
        verifyNoInteractions(chatClient);
    }

    @Test
    void saveQuestion_userWithoutPermission_throwsForbiddenException() {
        mockCurrentUserWithRole("KASUTAJA");

        assertThatThrownBy(() -> aiQuestionService.saveQuestion(saveRequest(singleChoice, answer("A", true), answer("B", false))))
                .isInstanceOf(ForbiddenException.class);

        verify(aiQuestionRepository, never()).save(any());
    }

    // --- Täpsustav küsimus ---

    @Test
    void generateQuestions_aiAsksClarifyingQuestion_returnsItWithoutQuestions() {
        mockCurrentUserWithRole("ADMIN");
        mockAvailableLevelsAndTypes();
        mockAiSelects(new QuestionTargets(List.of(), "Millise taseme jaoks?"));

        AiQuestionGenerationResponse response = aiQuestionService.generateQuestions(askRequest("Loo Java küsimus"));

        assertThat(response.getClarifyingQuestion()).isEqualTo("Millise taseme jaoks?");
        assertThat(response.getQuestions()).isEmpty();
    }

    @Test
    void generateQuestions_aiChoosesNonexistentLevel_returnsDefaultClarifyingQuestion() {
        mockCurrentUserWithRole("ADMIN");
        mockAvailableLevelsAndTypes();
        mockAiSelects(new QuestionTargets(List.of(new QuestionTarget(99, 1, 2)), null));

        AiQuestionGenerationResponse response = aiQuestionService.generateQuestions(askRequest("Loo Java küsimus"));

        assertThat(response.getClarifyingQuestion()).isEqualTo("Millise kompetentsi ja taseme jaoks küsimus luua?");
        assertThat(response.getQuestions()).isEmpty();
    }

    // --- Küsimuste arv ---

    @Test
    void generateQuestions_aiWantsTenQuestions_returnsAtMostFive() {
        mockCurrentUserWithRole("ADMIN");
        mockAvailableLevelsAndTypes();
        mockAiSelectsAndGenerates(singleChoice, 10,
                validQuestion("K1"), validQuestion("K2"), validQuestion("K3"), validQuestion("K4"),
                validQuestion("K5"), validQuestion("K6"), validQuestion("K7"));

        AiQuestionGenerationResponse response = aiQuestionService.generateQuestions(askRequest("Loo 10 Java küsimust"));

        assertThat(response.getQuestions()).hasSize(5);
    }

    @Test
    void generateQuestions_aiReturnsMoreThanRequested_returnsOnlyRequestedCount() {
        mockCurrentUserWithRole("ADMIN");
        mockAvailableLevelsAndTypes();
        mockAiSelectsAndGenerates(singleChoice, 2,
                validQuestion("K1"), validQuestion("K2"), validQuestion("K3"), validQuestion("K4"));

        AiQuestionGenerationResponse response = aiQuestionService.generateQuestions(askRequest("Loo 2 Java küsimust"));

        assertThat(response.getQuestions()).hasSize(2);
    }

    // --- AI vastuste kontroll: vigased küsimused jäetakse välja ---

    @Test
    void generateQuestions_answerRevealsCorrectness_questionExcluded() {
        mockCurrentUserWithRole("ADMIN");
        mockAvailableLevelsAndTypes();
        mockAiSelectsAndGenerates(singleChoice, 2,
                question("Vihjega", "Vali õige vastus.", answer("Java (õige)", true), answer("Python", false)),
                validQuestion("Korrektne"));

        AiQuestionGenerationResponse response = aiQuestionService.generateQuestions(askRequest("Loo 2 Java küsimust"));

        assertThat(response.getQuestions()).extracting("title").containsExactly("Korrektne");
    }

    @Test
    void generateQuestions_singleChoiceWithTwoCorrectAnswers_questionExcluded() {
        mockCurrentUserWithRole("ADMIN");
        mockAvailableLevelsAndTypes();
        mockAiSelectsAndGenerates(singleChoice, 2,
                question("Kaks õiget", "Vali õige vastus.", answer("A", true), answer("B", true), answer("C", false)),
                validQuestion("Korrektne"));

        AiQuestionGenerationResponse response = aiQuestionService.generateQuestions(askRequest("Loo 2 Java küsimust"));

        assertThat(response.getQuestions()).extracting("title").containsExactly("Korrektne");
    }

    @Test
    void generateQuestions_moreThanFiveAnswers_questionExcluded() {
        mockCurrentUserWithRole("ADMIN");
        mockAvailableLevelsAndTypes();
        mockAiSelectsAndGenerates(multipleChoice, 2,
                question("Kuus vastust", "Vali õiged vastused.", answer("A", true), answer("B", false),
                        answer("C", false), answer("D", false), answer("E", false), answer("F", false)),
                question("Korrektne", "Vali õiged vastused.", answer("A", true), answer("B", true), answer("C", false), answer("D", false)));

        AiQuestionGenerationResponse response = aiQuestionService.generateQuestions(askRequest("Loo 2 Java küsimust"));

        assertThat(response.getQuestions()).extracting("title").containsExactly("Korrektne");
    }

    @Test
    void generateQuestions_descriptionLongerThanTwelveLines_questionExcluded() {
        mockCurrentUserWithRole("ADMIN");
        mockAvailableLevelsAndTypes();
        String thirteenLines = "rida\n".repeat(13);
        mockAiSelectsAndGenerates(singleChoice, 2,
                question("Pikk kirjeldus", thirteenLines, answer("A", true), answer("B", false)),
                validQuestion("Korrektne"));

        AiQuestionGenerationResponse response = aiQuestionService.generateQuestions(askRequest("Loo 2 Java küsimust"));

        assertThat(response.getQuestions()).extracting("title").containsExactly("Korrektne");
    }

    @Test
    void generateQuestions_allQuestionsInvalid_throwsBadRequestException() {
        mockCurrentUserWithRole("ADMIN");
        mockAvailableLevelsAndTypes();
        mockAiSelectsAndGenerates(singleChoice, 1,
                question("Vihjega", "Vali õige vastus.", answer("Java ✓", true), answer("Python", false)));

        assertThatThrownBy(() -> aiQuestionService.generateQuestions(askRequest("Loo Java küsimus")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void generateQuestions_trueFalseWithThreeAnswers_questionExcluded() {
        mockCurrentUserWithRole("ADMIN");
        mockAvailableLevelsAndTypes();
        mockAiSelectsAndGenerates(trueFalse, 2,
                question("Kolm vastust", "Vali õige vastus.", answer("Tõene", true), answer("Väär", false), answer("Ei tea", false)),
                question("Korrektne", "Vali õige vastus.", answer("Tõene", true), answer("Väär", false)));

        AiQuestionGenerationResponse response = aiQuestionService.generateQuestions(askRequest("Loo 2 tõene/väär küsimust"));

        assertThat(response.getQuestions()).extracting("title").containsExactly("Korrektne");
    }

    // --- Salvestamine ---

    @Test
    void saveQuestion_validQuestion_savesAsPendingWithCurrentUserAsAuthor() {
        mockCurrentUserWithRole("HALDUR");
        when(competenceLevelRepository.findById(COMPETENCE_LEVEL_ID)).thenReturn(Optional.of(competenceLevel));
        when(questionTypeRepository.findById(singleChoice.getId())).thenReturn(Optional.of(singleChoice));
        when(aiQuestionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        aiQuestionService.saveQuestion(saveRequest(singleChoice, answer("A", true), answer("B", false)));

        ArgumentCaptor<AiQuestion> aiQuestionCaptor = ArgumentCaptor.forClass(AiQuestion.class);
        verify(aiQuestionRepository).save(aiQuestionCaptor.capture());
        AiQuestion aiQuestion = aiQuestionCaptor.getValue();
        assertThat(aiQuestion.getStatus()).isEqualTo("P");
        assertThat(aiQuestion.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(aiQuestion.getCompetenceLevel()).isEqualTo(competenceLevel);

        ArgumentCaptor<AiQuestionAnswer> aiQuestionAnswerCaptor = ArgumentCaptor.forClass(AiQuestionAnswer.class);
        verify(aiQuestionAnswerRepository, times(2)).save(aiQuestionAnswerCaptor.capture());
        List<AiQuestionAnswer> aiQuestionAnswers = new ArrayList<>(aiQuestionAnswerCaptor.getAllValues());
        assertThat(aiQuestionAnswers).extracting("status").containsOnly("P");
        assertThat(aiQuestionAnswers).extracting("isCorrect").containsExactly(true, false);
        assertThat(aiQuestionAnswers).extracting("aiQuestion").containsOnly(aiQuestion);
    }

    @Test
    void saveQuestion_answerRevealsCorrectness_throwsBadRequestAndSavesNothing() {
        mockCurrentUserWithRole("ADMIN");
        when(competenceLevelRepository.findById(COMPETENCE_LEVEL_ID)).thenReturn(Optional.of(competenceLevel));
        when(questionTypeRepository.findById(singleChoice.getId())).thenReturn(Optional.of(singleChoice));

        assertThatThrownBy(() -> aiQuestionService.saveQuestion(saveRequest(singleChoice, answer("A (õige)", true), answer("B", false))))
                .isInstanceOf(BadRequestException.class);

        verify(aiQuestionRepository, never()).save(any());
        verify(aiQuestionAnswerRepository, never()).save(any());
    }

    @Test
    void saveQuestion_trueFalseWithThreeAnswers_throwsBadRequestAndSavesNothing() {
        mockCurrentUserWithRole("ADMIN");
        when(competenceLevelRepository.findById(COMPETENCE_LEVEL_ID)).thenReturn(Optional.of(competenceLevel));
        when(questionTypeRepository.findById(trueFalse.getId())).thenReturn(Optional.of(trueFalse));

        assertThatThrownBy(() -> aiQuestionService.saveQuestion(
                saveRequest(trueFalse, answer("Tõene", true), answer("Väär", false), answer("Ei tea", false))))
                .isInstanceOf(BadRequestException.class);

        verify(aiQuestionRepository, never()).save(any());
    }

}
