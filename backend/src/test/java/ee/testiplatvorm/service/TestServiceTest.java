package ee.testiplatvorm.service;

import ee.testiplatvorm.controller.test.dto.TestCreateRequestDto;
import ee.testiplatvorm.controller.test.dto.TestQuestionDto;
import ee.testiplatvorm.infrastructure.exception.ForbiddenException;
import ee.testiplatvorm.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.testiplatvorm.persistence.ProfileRepository;
import ee.testiplatvorm.persistence.Role;
import ee.testiplatvorm.persistence.competencelevel.CompetenceLevel;
import ee.testiplatvorm.persistence.competencelevel.CompetenceLevelRepository;
import ee.testiplatvorm.persistence.question.Question;
import ee.testiplatvorm.persistence.question.QuestionRepository;
import ee.testiplatvorm.persistence.test.TestMapper;
import ee.testiplatvorm.persistence.test.TestRepository;
import ee.testiplatvorm.persistence.testquestion.TestQuestion;
import ee.testiplatvorm.persistence.testquestion.TestQuestionRepository;
import ee.testiplatvorm.persistence.user.User;
import ee.testiplatvorm.persistence.user.UserRepository;
import ee.testiplatvorm.persistence.usertest.UserTestRepository;
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

// NB! Entiteet ee.testiplatvorm.persistence.test.Test on sama nimega kui JUniti @Test,
// seepärast kasutame entiteeti täisnimega ja @Test on JUniti oma.
@ExtendWith(MockitoExtension.class)
class TestServiceTest {

    // Kõik TestService konstruktori sõltuvused on mockid
    @Mock
    private TestRepository testRepository;
    @Mock
    private TestMapper testMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CompetenceLevelRepository competenceLevelRepository;
    @Mock
    private UserTestRepository userTestRepository;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private TestQuestionRepository testQuestionRepository;
    @Mock
    private UserService userService;
    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private TestService testService;

    @Test
    void createTest_adminWithThreeQuestions_savesActiveTestAndQuestionsWithPositions() {
        // Arrange
        // 1. Kasutaja, kelle roll on ADMIN
        Role role = new Role();
        role.setName("ADMIN");
        User user = new User();
        user.setRole(role);

        // 2. Kompetentsi tase
        CompetenceLevel competenceLevel = new CompetenceLevel();

        // 3. Küsimused, mis testi lisatakse
        Question questionA = new Question();
        Question questionB = new Question();
        Question questionC = new Question();

        // 4. Request DTO – küsimused järjekorras 10, 20, 30
        TestQuestionDto testQuestionDtoA = new TestQuestionDto();
        testQuestionDtoA.setQuestionId(10);
        TestQuestionDto testQuestionDtoB = new TestQuestionDto();
        testQuestionDtoB.setQuestionId(20);
        TestQuestionDto testQuestionDtoC = new TestQuestionDto();
        testQuestionDtoC.setQuestionId(30);

        TestCreateRequestDto testCreateRequestDto = new TestCreateRequestDto();
        testCreateRequestDto.setCompetenceLevelId(2);
        testCreateRequestDto.setQuestions(List.of(testQuestionDtoA, testQuestionDtoB, testQuestionDtoC));

        // 5. Testi entiteet, mille mapper "loob"
        ee.testiplatvorm.persistence.test.Test test = new ee.testiplatvorm.persistence.test.Test();

        // 6. Mock'id
        when(currentUserService.getUserId()).thenReturn(5);
        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        when(testMapper.toTest(testCreateRequestDto)).thenReturn(test);
        when(competenceLevelRepository.findById(2)).thenReturn(Optional.of(competenceLevel));
        when(testRepository.save(test)).thenReturn(test);
        when(questionRepository.findById(10)).thenReturn(Optional.of(questionA));
        when(questionRepository.findById(20)).thenReturn(Optional.of(questionB));
        when(questionRepository.findById(30)).thenReturn(Optional.of(questionC));

        // Act
        testService.createTest(testCreateRequestDto);

        // Assert
        // Test salvestati aktiivsena ja looja on meie kasutaja
        verify(testRepository).save(test);
        assertThat(test.getStatus()).isEqualTo("A");
        assertThat(test.getCreatedBy()).isEqualTo(user);
        assertThat(test.getCompetenceLevel()).isEqualTo(competenceLevel);

        // ArgumentCaptor püüab kinni objektid, mis save()-ile anti, et saaks nende sisu kontrollida
        ArgumentCaptor<TestQuestion> testQuestionCaptor = ArgumentCaptor.forClass(TestQuestion.class);
        verify(testQuestionRepository, times(3)).save(testQuestionCaptor.capture());
        List<TestQuestion> savedTestQuestions = testQuestionCaptor.getAllValues();

        assertThat(savedTestQuestions.get(0).getQuestion()).isEqualTo(questionA);
        assertThat(savedTestQuestions.get(0).getPosition()).isEqualTo(1);
        assertThat(savedTestQuestions.get(1).getQuestion()).isEqualTo(questionB);
        assertThat(savedTestQuestions.get(1).getPosition()).isEqualTo(2);
        assertThat(savedTestQuestions.get(2).getQuestion()).isEqualTo(questionC);
        assertThat(savedTestQuestions.get(2).getPosition()).isEqualTo(3);
    }

    @Test
    void createTest_userWithoutPermission_throwsForbiddenException() {
        // Arrange
        // 1. Kasutaja, kelle roll ei ole ADMIN ega HALDUR
        Role role = new Role();
        role.setName("KASUTAJA");
        User user = new User();
        user.setRole(role);

        // 2. Request DTO – sisu pole oluline, sest õiguste kontroll kukub enne
        TestCreateRequestDto testCreateRequestDto = new TestCreateRequestDto();

        // 3. Mock'id – kasutaja ID tuleb sessioonist
        when(currentUserService.getUserId()).thenReturn(5);
        when(userRepository.findById(5)).thenReturn(Optional.of(user));

        // Act + Assert
        assertThatThrownBy(() -> testService.createTest(testCreateRequestDto))
                .isInstanceOf(ForbiddenException.class);

        verify(testRepository, never()).save(any());
        verify(testQuestionRepository, never()).save(any());
    }

    @Test
    void createTest_questionDoesNotExist_throwsPrimaryKeyNotFoundException() {
        // Arrange
        // 1. Kasutaja, kelle roll on ADMIN
        Role role = new Role();
        role.setName("ADMIN");
        User user = new User();
        user.setRole(role);

        // 2. Kompetentsi tase
        CompetenceLevel competenceLevel = new CompetenceLevel();

        // 3. Request DTO – küsimust ID-ga 99 ei ole olemas
        TestQuestionDto testQuestionDto = new TestQuestionDto();
        testQuestionDto.setQuestionId(99);

        TestCreateRequestDto testCreateRequestDto = new TestCreateRequestDto();
        testCreateRequestDto.setCompetenceLevelId(2);
        testCreateRequestDto.setQuestions(List.of(testQuestionDto));

        ee.testiplatvorm.persistence.test.Test test = new ee.testiplatvorm.persistence.test.Test();

        // 4. Mock'id – questionRepository tagastab tühja karbi
        when(currentUserService.getUserId()).thenReturn(5);
        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        when(testMapper.toTest(testCreateRequestDto)).thenReturn(test);
        when(competenceLevelRepository.findById(2)).thenReturn(Optional.of(competenceLevel));
        when(testRepository.save(test)).thenReturn(test);
        when(questionRepository.findById(99)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> testService.createTest(testCreateRequestDto))
                .isInstanceOf(PrimaryKeyNotFoundException.class);

        // Test ise salvestatakse enne küsimuste kontrolli (päris rakenduses @Transactional tühistab selle),
        // aga küsimuse seost ei tohi salvestada
        verify(testQuestionRepository, never()).save(any());
    }

}
