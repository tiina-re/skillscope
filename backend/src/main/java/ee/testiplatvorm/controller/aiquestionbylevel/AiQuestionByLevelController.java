package ee.testiplatvorm.controller.aiquestionbylevel;

import ee.testiplatvorm.controller.aiquestionbylevel.dto.QuestionsRequest;
import ee.testiplatvorm.service.AiQuestionByLevelService;
import ee.testiplatvorm.controller.aiquestion.dto.AiQuestionGenerationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AiQuestionByLevelController {

    private final AiQuestionByLevelService aiQuestionByLevelService;

    @PostMapping("/api/ai-questions/generate-by-level")
    public AiQuestionGenerationResponse generateQuestionsByCompetenceAndLevel(@Valid @RequestBody QuestionsRequest questionsRequest) {
        return aiQuestionByLevelService.generateQuestionsByCompetenceAndLevel(questionsRequest);
    }
}
