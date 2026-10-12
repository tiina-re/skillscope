package ee.testiplatvorm.service;

import ee.testiplatvorm.controller.aiquestion.dto.GeneratedAnswerDto;
import ee.testiplatvorm.persistence.questiontype.QuestionType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

// AI loodud küsimuste ühised reeglid - kasutavad nii AI chat (AiQuestionService) kui ka taseme põhine loomine (AiQuestionByLevelService)
@Service
public class AiQuestionValidationService {

    public static final int MAX_ANSWER_COUNT = 5;
    private static final int MIN_ANSWER_COUNT = 2;
    // Kirjelduses on lubatud kuni 10-realine koodinäide + paar rida juhist
    private static final int MAX_DESCRIPTION_LINE_COUNT = 12;
    // Markdowni koodiploki piirid (```javascript ja ```) ei ole koodiread, neid ridade arvu ei loeta
    private static final String CODE_FENCE = "```";

    // Märgendid, mis reedavad vastuse õigsuse, nt "(õige)", "- vale", "✓"
    private static final Pattern ANSWER_CORRECTNESS_HINT = Pattern.compile(
            "[✓✔✗✘]|\\b(õige|vale|correct|wrong|incorrect)\\s*(vastus|answer)?\\s*[)\\]]|[(\\[]\\s*(õige|vale|correct|wrong|incorrect)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    public boolean isValidQuestion(String title, String description, List<GeneratedAnswerDto> answers, QuestionType questionType) {
        return findInvalidQuestionReason(title, description, answers, questionType).isEmpty();
    }

    // Tagastab põhjuse, miks küsimus reeglitele ei vasta (logimiseks), või tühja tulemuse, kui küsimus sobib
    public Optional<String> findInvalidQuestionReason(String title, String description, List<GeneratedAnswerDto> answers,
                                                      QuestionType questionType) {
        if (title == null || title.isBlank()) {
            return Optional.of("pealkiri puudub");
        }
        if (answers == null || answers.size() < MIN_ANSWER_COUNT || answers.size() > MAX_ANSWER_COUNT) {
            return Optional.of("vastuste arv %d (lubatud %d-%d)".formatted(answers == null ? 0 : answers.size(),
                    MIN_ANSWER_COUNT, MAX_ANSWER_COUNT));
        }
        // Tõene/väär küsimusel on alati täpselt 2 vastust, nagu QuestionService.createQuestion-is
        if (questionType.getName().equals("TRUE_FALSE") && answers.size() != 2) {
            return Optional.of("tõene/väär küsimusel %d vastust (peab olema 2)".formatted(answers.size()));
        }

        long descriptionLineCount = countDescriptionLines(description);
        if (descriptionLineCount > MAX_DESCRIPTION_LINE_COUNT) {
            return Optional.of("kirjelduses %d rida (lubatud %d)".formatted(descriptionLineCount, MAX_DESCRIPTION_LINE_COUNT));
        }

        boolean hasCorrectnessHint = answers.stream()
                .anyMatch(answer -> answer.getAnswerText() == null
                        || ANSWER_CORRECTNESS_HINT.matcher(answer.getAnswerText()).find());
        if (hasCorrectnessHint) {
            return Optional.of("vastus on tühi või reedab õigsuse (nt \"(õige)\")");
        }

        long correctAnswerCount = answers.stream()
                .filter(answer -> Boolean.TRUE.equals(answer.getIsCorrect()))
                .count();
        boolean isMultipleChoice = questionType.getName().equals("MULTIPLE_CHOICE");
        boolean hasValidCorrectAnswerCount = isMultipleChoice ? correctAnswerCount >= 1 : correctAnswerCount == 1;
        if (!hasValidCorrectAnswerCount) {
            return Optional.of("õigeid vastuseid %d tüübile %s".formatted(correctAnswerCount, questionType.getName()));
        }
        return Optional.empty();
    }

    private long countDescriptionLines(String description) {
        if (description == null) {
            return 0;
        }
        return description.lines()
                .filter(line -> !line.strip().startsWith(CODE_FENCE))
                .count();
    }
}
