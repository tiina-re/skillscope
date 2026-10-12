package ee.testiplatvorm.controller.aiquestion;

import ee.testiplatvorm.controller.aiquestion.dto.AiQuestionBankDto;
import ee.testiplatvorm.controller.aiquestion.dto.AiQuestionGenerationResponse;
import ee.testiplatvorm.controller.aiquestion.dto.AiQuestionReviewRequestDto;
import ee.testiplatvorm.controller.aiquestion.dto.AiQuestionSaveRequest;
import ee.testiplatvorm.controller.aiquestion.dto.AskRequest;
import ee.testiplatvorm.service.AiQuestionBankService;
import ee.testiplatvorm.service.AiQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AiQuestionController {

    private final AiQuestionService aiQuestionService;
    private final AiQuestionBankService aiQuestionBankService;

    @GetMapping("/api/ai-questions")
    @Operation(summary = "Tagastab AI küsimused koos vastusevariantidega, uuemad eespool. status (P = ootab ülevaatust, A = kinnitatud, R = tagasi lükatud) ja competenceId on valikulised - kui need puuduvad, tagastatakse kõik AI küsimused.")
    @ApiResponse(responseCode = "200", description = "OK")
    public List<AiQuestionBankDto> findAllAiQuestionsBy(@RequestParam(required = false) String status,
                                                        @RequestParam(required = false) Integer competenceId) {
        List<AiQuestionBankDto> aiQuestionBankDtos = aiQuestionBankService.findAllAiQuestionsBy(status, competenceId);
        return aiQuestionBankDtos;
    }

    @PostMapping("/api/ai-questions/{aiQuestionId}/review")
    @Operation(summary = "AI küsimuse ülevaatus: salvestab hinnangu (score 1-5), tagasiside (feedback) ja otsuse (is_good). Kinnitamisel (approved = true) saab AI küsimus staatuse 'A' ja kopeeritakse koos vastustega küsimuste panka (question, question_answer). Tagasilükkamisel saab staatuse 'R'.")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Vigane sisend (INCORRECT_INPUT) või AI küsimus on juba üle vaadatud (AI_QUESTION_ALREADY_REVIEWED)")
    @ApiResponse(responseCode = "404", description = "aiQuestionId ei leitud (PRIMARY_KEY_NOT_FOUND)")
    public void reviewAiQuestion(@PathVariable Integer aiQuestionId,
                                 @RequestBody @Valid AiQuestionReviewRequestDto aiQuestionReviewRequestDto) {
        aiQuestionBankService.reviewAiQuestion(aiQuestionId, aiQuestionReviewRequestDto);
    }

    @PostMapping("/api/ai-questions/generate")
    @Operation(summary = "AI tuvastab vabatekstist kompetentsi, taseme, küsimuse tüübi ja küsimuste arvu (kuni 5) ning genereerib küsimused koos vastusevariantidega. Midagi ei salvestata - salvestamiseks tuleb kasutada POST /api/ai-questions. Kui kompetentsi või taset ei õnnestu tuvastada, tagastatakse ainult clarifyingQuestion.")
    @ApiResponse(responseCode = "200", description = "OK - kas genereeritud küsimused (questions) või täpsustav küsimus (clarifyingQuestion)")
    @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud")
    @ApiResponse(responseCode = "403", description = "Kasutajal puudub õigus küsimusi luua (lubatud ainult ADMIN ja HALDUR rollile)")
    public AiQuestionGenerationResponse generateQuestions(@RequestBody @Valid AskRequest askRequest) {
        AiQuestionGenerationResponse aiQuestionGenerationResponse = aiQuestionService.generateQuestions(askRequest);
        return aiQuestionGenerationResponse;
    }

    @PostMapping("/api/ai-questions")
    @Operation(summary = "Salvestab kasutaja kinnitatud AI küsimuse ai_question ja ai_question_answer tabelitesse staatusega 'P' (ootab ülevaatust). Tagastab loodud ai_question id.")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Vigased sisendandmed või küsimus ei vasta reeglitele")
    @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud")
    @ApiResponse(responseCode = "403", description = "Kasutajal puudub õigus küsimusi luua (lubatud ainult ADMIN ja HALDUR rollile)")
    @ApiResponse(responseCode = "404", description = "competenceLevelId või questionTypeId ei leitud")
    public Integer saveQuestion(@RequestBody @Valid AiQuestionSaveRequest aiQuestionSaveRequest) {
        return aiQuestionService.saveQuestion(aiQuestionSaveRequest);
    }
}
