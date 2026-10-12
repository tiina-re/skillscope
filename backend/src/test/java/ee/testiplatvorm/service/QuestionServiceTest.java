package ee.testiplatvorm.service;

import ee.testiplatvorm.controller.question.dto.QuestionCreateAnswerRequestDto;
import ee.testiplatvorm.controller.question.dto.QuestionCreateRequestDto;
import ee.testiplatvorm.infrastructure.exception.BadRequestException;
import ee.testiplatvorm.infrastructure.exception.ForbiddenException;
import ee.testiplatvorm.persistence.Role;
import ee.testiplatvorm.persistence.competencelevel.CompetenceLevel;
import ee.testiplatvorm.persistence.competencelevel.CompetenceLevelRepository;
import ee.testiplatvorm.persistence.question.Question;
import ee.testiplatvorm.persistence.question.QuestionMapper;
import ee.testiplatvorm.persistence.question.QuestionRepository;
import ee.testiplatvorm.persistence.questionanswer.QuestionAnswerMapper;
import ee.testiplatvorm.persistence.questionanswer.QuestionAnswerRepository;
import ee.testiplatvorm.persistence.questiontype.QuestionType;
import ee.testiplatvorm.persistence.questiontype.QuestionTypeRepository;
import ee.testiplatvorm.persistence.user.User;
import ee.testiplatvorm.persistence.user.UserMapper;
import ee.testiplatvorm.persistence.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    // Kõik QuestionService konstruktori sõltuvused on mockid — päris andmebaasi ega Springi ei käivitata
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private QuestionMapper questionMapper;
    @Mock
    private QuestionAnswerRepository questionAnswerRepository;
    @Mock
    private QuestionAnswerMapper questionAnswerMapper;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private QuestionTypeRepository questionTypeRepository;
    @Mock
    private CompetenceLevelRepository competenceLevelRepository;

    // Päris QuestionService, millele Mockito annab ülalolevad mockid konstruktori kaudu
    @InjectMocks
    private QuestionService questionService;

    @Test
    void createQuestion_singleChoiceWithOneCorrectAnswer_savesQuestion() {
        // Arrange
        // 1. Kasutaja, kelle roll on ADMIN
        Role role = new Role();
        role.setName("ADMIN");
        User user = new User();
        user.setRole(role);

        // 2. Küsimuse tüüp
        QuestionType questionType = new QuestionType();
        questionType.setName("SINGLE_CHOICE");

        // 3. Kompetentsi tase
        CompetenceLevel competenceLevel = new CompetenceLevel();

        // 4. Request DTO
        QuestionCreateAnswerRequestDto correctAnswer = new QuestionCreateAnswerRequestDto("Vastus A", true);
        QuestionCreateAnswerRequestDto wrongAnswer = new QuestionCreateAnswerRequestDto("Vastus B", false);
        List<QuestionCreateAnswerRequestDto> questionCreateAnswerRequestDtos = List.of(correctAnswer, wrongAnswer);

        QuestionCreateRequestDto questionCreateRequestDto = new QuestionCreateRequestDto();
        questionCreateRequestDto.setQuestionTypeId(1);
        questionCreateRequestDto.setCompetenceLevelId(2);
        questionCreateRequestDto.setAnswers(questionCreateAnswerRequestDtos);

        // 5. Mock'id
        when(currentUserService.getUserId()).thenReturn(5);
        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        when(questionTypeRepository.findById(1)).thenReturn(Optional.of(questionType));
        when(competenceLevelRepository.findById(2)).thenReturn(Optional.of(competenceLevel));
        when(questionMapper.toQuestion(questionCreateRequestDto)).thenReturn(new Question());

        // Act
        questionService.createQuestion(questionCreateRequestDto);

        // Assert
        verify(questionRepository).save(any());
        verify(questionAnswerRepository).saveAll(any());
    }

    @Test
    void createQuestion_singleChoiceWithZeroCorrectAnswers_throwsBadRequestException() {
        // Arrange
        // 1. Kasutaja, kelle roll on ADMIN
        Role role = new Role();
        role.setName("ADMIN");
        User user = new User();
        user.setRole(role);

        // 2. Küsimuse tüüp
        QuestionType questionType = new QuestionType();
        questionType.setName("SINGLE_CHOICE");

        // 3. Kompetentsi tase
        CompetenceLevel competenceLevel = new CompetenceLevel();

        // 4. Request DTO
        QuestionCreateAnswerRequestDto wrongAnswerA = new QuestionCreateAnswerRequestDto("Vastus A", false);
        QuestionCreateAnswerRequestDto wrongAnswerB = new QuestionCreateAnswerRequestDto("Vastus B", false);
        List<QuestionCreateAnswerRequestDto> questionCreateAnswerRequestDtos = List.of(wrongAnswerA, wrongAnswerB);

        QuestionCreateRequestDto questionCreateRequestDto = new QuestionCreateRequestDto();
        questionCreateRequestDto.setQuestionTypeId(1);
        questionCreateRequestDto.setCompetenceLevelId(2);
        questionCreateRequestDto.setAnswers(questionCreateAnswerRequestDtos);

        // 5. Mock'id
        when(currentUserService.getUserId()).thenReturn(5);
        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        when(questionTypeRepository.findById(1)).thenReturn(Optional.of(questionType));
        when(competenceLevelRepository.findById(2)).thenReturn(Optional.of(competenceLevel));

        // Act
        assertThatThrownBy(() -> questionService.createQuestion(questionCreateRequestDto))
                .isInstanceOf(BadRequestException.class);

        // Assert
        verify(questionRepository, never()).save(any());
        verify(questionAnswerRepository, never()).saveAll(any());
    }

    @Test
    void createQuestion_singleChoiceWithTwoCorrectAnswers_throwsBadRequestException() {
        // Arrange
        // 1. Kasutaja, kelle roll on ADMIN
        Role role = new Role();
        role.setName("ADMIN");
        User user = new User();
        user.setRole(role);

        // 2. Küsimuse tüüp
        QuestionType questionType = new QuestionType();
        questionType.setName("SINGLE_CHOICE");

        // 3. Kompetentsi tase
        CompetenceLevel competenceLevel = new CompetenceLevel();

        // 4. Request DTO
        QuestionCreateAnswerRequestDto correctAnswerA = new QuestionCreateAnswerRequestDto("Vastus A", true);
        QuestionCreateAnswerRequestDto correctAnswerB = new QuestionCreateAnswerRequestDto("Vastus B", true);
        List<QuestionCreateAnswerRequestDto> questionCreateAnswerRequestDtos = List.of(correctAnswerA, correctAnswerB);

        QuestionCreateRequestDto questionCreateRequestDto = new QuestionCreateRequestDto();
        questionCreateRequestDto.setQuestionTypeId(1);
        questionCreateRequestDto.setCompetenceLevelId(2);
        questionCreateRequestDto.setAnswers(questionCreateAnswerRequestDtos);

        // 5. Mock'id
        when(currentUserService.getUserId()).thenReturn(5);
        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        when(questionTypeRepository.findById(1)).thenReturn(Optional.of(questionType));
        when(competenceLevelRepository.findById(2)).thenReturn(Optional.of(competenceLevel));

        // Act + Assert
        assertThatThrownBy(() -> questionService.createQuestion(questionCreateRequestDto))
                .isInstanceOf(BadRequestException.class);

        verify(questionRepository, never()).save(any());
        verify(questionAnswerRepository, never()).saveAll(any());
    }

    @Test
    void createQuestion_multipleChoiceWithTwoCorrectAnswers_savesQuestion() {
        // Arrange
        // 1. Kasutaja, kelle roll on ADMIN
        Role role = new Role();
        role.setName("ADMIN");
        User user = new User();
        user.setRole(role);

        // 2. Küsimuse tüüp
        QuestionType questionType = new QuestionType();
        questionType.setName("MULTIPLE_CHOICE");

        // 3. Kompetentsi tase
        CompetenceLevel competenceLevel = new CompetenceLevel();

        // 4. Request DTO
        QuestionCreateAnswerRequestDto correctAnswerA = new QuestionCreateAnswerRequestDto("Vastus A", true);
        QuestionCreateAnswerRequestDto correctAnswerB = new QuestionCreateAnswerRequestDto("Vastus B", true);
        QuestionCreateAnswerRequestDto wrongAnswer = new QuestionCreateAnswerRequestDto("Vastus C", false);
        List<QuestionCreateAnswerRequestDto> questionCreateAnswerRequestDtos = List.of(correctAnswerA, correctAnswerB, wrongAnswer);

        QuestionCreateRequestDto questionCreateRequestDto = new QuestionCreateRequestDto();
        questionCreateRequestDto.setQuestionTypeId(1);
        questionCreateRequestDto.setCompetenceLevelId(2);
        questionCreateRequestDto.setAnswers(questionCreateAnswerRequestDtos);

        // 5. Mock'id
        when(currentUserService.getUserId()).thenReturn(5);
        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        when(questionTypeRepository.findById(1)).thenReturn(Optional.of(questionType));
        when(competenceLevelRepository.findById(2)).thenReturn(Optional.of(competenceLevel));
        when(questionMapper.toQuestion(questionCreateRequestDto)).thenReturn(new Question());

        // Act
        questionService.createQuestion(questionCreateRequestDto);

        // Assert
        verify(questionRepository).save(any());
        verify(questionAnswerRepository).saveAll(any());
    }

    @Test
    void createQuestion_multipleChoiceWithZeroCorrectAnswers_throwsBadRequestException() {
        // Arrange
        // 1. Kasutaja, kelle roll on ADMIN
        Role role = new Role();
        role.setName("ADMIN");
        User user = new User();
        user.setRole(role);

        // 2. Küsimuse tüüp
        QuestionType questionType = new QuestionType();
        questionType.setName("MULTIPLE_CHOICE");

        // 3. Kompetentsi tase
        CompetenceLevel competenceLevel = new CompetenceLevel();

        // 4. Request DTO
        QuestionCreateAnswerRequestDto wrongAnswerA = new QuestionCreateAnswerRequestDto("Vastus A", false);
        QuestionCreateAnswerRequestDto wrongAnswerB = new QuestionCreateAnswerRequestDto("Vastus B", false);
        List<QuestionCreateAnswerRequestDto> questionCreateAnswerRequestDtos = List.of(wrongAnswerA, wrongAnswerB);

        QuestionCreateRequestDto questionCreateRequestDto = new QuestionCreateRequestDto();
        questionCreateRequestDto.setQuestionTypeId(1);
        questionCreateRequestDto.setCompetenceLevelId(2);
        questionCreateRequestDto.setAnswers(questionCreateAnswerRequestDtos);

        // 5. Mock'id
        when(currentUserService.getUserId()).thenReturn(5);
        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        when(questionTypeRepository.findById(1)).thenReturn(Optional.of(questionType));
        when(competenceLevelRepository.findById(2)).thenReturn(Optional.of(competenceLevel));

        // Act + Assert
        assertThatThrownBy(() -> questionService.createQuestion(questionCreateRequestDto))
                .isInstanceOf(BadRequestException.class);

        verify(questionRepository, never()).save(any());
        verify(questionAnswerRepository, never()).saveAll(any());
    }

    @Test
    void createQuestion_trueFalseWithThreeAnswers_throwsBadRequestException() {
        // Arrange
        // 1. Kasutaja, kelle roll on ADMIN
        Role role = new Role();
        role.setName("ADMIN");
        User user = new User();
        user.setRole(role);

        // 2. Küsimuse tüüp
        QuestionType questionType = new QuestionType();
        questionType.setName("TRUE_FALSE");

        // 3. Kompetentsi tase
        CompetenceLevel competenceLevel = new CompetenceLevel();

        // 4. Request DTO – 1 õige vastus, aga vastuseid on 3
        QuestionCreateAnswerRequestDto correctAnswer = new QuestionCreateAnswerRequestDto("Tõene", true);
        QuestionCreateAnswerRequestDto wrongAnswerA = new QuestionCreateAnswerRequestDto("Väär", false);
        QuestionCreateAnswerRequestDto wrongAnswerB = new QuestionCreateAnswerRequestDto("Ei tea", false);
        List<QuestionCreateAnswerRequestDto> questionCreateAnswerRequestDtos = List.of(correctAnswer, wrongAnswerA, wrongAnswerB);

        QuestionCreateRequestDto questionCreateRequestDto = new QuestionCreateRequestDto();
        questionCreateRequestDto.setQuestionTypeId(1);
        questionCreateRequestDto.setCompetenceLevelId(2);
        questionCreateRequestDto.setAnswers(questionCreateAnswerRequestDtos);

        // 5. Mock'id
        when(currentUserService.getUserId()).thenReturn(5);
        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        when(questionTypeRepository.findById(1)).thenReturn(Optional.of(questionType));
        when(competenceLevelRepository.findById(2)).thenReturn(Optional.of(competenceLevel));

        // Act + Assert
        assertThatThrownBy(() -> questionService.createQuestion(questionCreateRequestDto))
                .isInstanceOf(BadRequestException.class);

        verify(questionRepository, never()).save(any());
        verify(questionAnswerRepository, never()).saveAll(any());
    }

    @Test
    void createQuestion_userWithoutPermission_throwsForbiddenException() {
        // Arrange
        // 1. Kasutaja, kelle roll ei ole ADMIN ega HALDUR
        Role role = new Role();
        role.setName("KASUTAJA");
        User user = new User();
        user.setRole(role);

        // 2. Request DTO – sisu pole oluline, sest õiguste kontroll kukub enne
        QuestionCreateRequestDto questionCreateRequestDto = new QuestionCreateRequestDto();

        // 3. Mock'id – ainult need, milleni kood jõuab enne õiguste kontrolli
        when(currentUserService.getUserId()).thenReturn(5);
        when(userRepository.findById(5)).thenReturn(Optional.of(user));

        // Act + Assert
        assertThatThrownBy(() -> questionService.createQuestion(questionCreateRequestDto))
                .isInstanceOf(ForbiddenException.class);

        verify(questionRepository, never()).save(any());
        verify(questionAnswerRepository, never()).saveAll(any());
    }

}
