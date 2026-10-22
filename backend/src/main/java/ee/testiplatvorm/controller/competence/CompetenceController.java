package ee.testiplatvorm.controller.competence;

import ee.testiplatvorm.controller.competence.dto.CompetenceDetailDto;
import ee.testiplatvorm.controller.competence.dto.CompetenceResponseDto;
import ee.testiplatvorm.service.CompetenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CompetenceController {

    private final CompetenceService competenceService;

    @GetMapping("/api/competences")
    @Operation(summary = "Tagastatakse ainult aktiivsed kompetentsid (competence tabeli status = 'A').")
    @ApiResponse(
            responseCode = "200", description = "OK"
    )
    public List<CompetenceResponseDto> findCompetences() {

        return competenceService.findCompetences();
    }

    @GetMapping("/api/competences/{competenceId}")
    @Operation(summary = "Tagastatakse kompetentsi nimi, lühikirjeldus ja kirjeldus.")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "competenceId järgi rida ei leitud (PRIMARY_KEY_NOT_FOUND)")
    public CompetenceDetailDto findCompetenceDetail(@PathVariable Integer competenceId) {
        return competenceService.findCompetenceDetail(competenceId);
    }
}
