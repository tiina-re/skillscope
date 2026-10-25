package ee.testiplatvorm.controller.test;


import ee.testiplatvorm.controller.test.dto.TestCreateRequestDto;
import ee.testiplatvorm.controller.test.dto.TestDetailDto;
import ee.testiplatvorm.controller.test.dto.TestStartDto;
import ee.testiplatvorm.controller.test.dto.TestSummaryDto;
import ee.testiplatvorm.infrastructure.error.ApiError;
import ee.testiplatvorm.service.TestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TestController {

    private final TestService testService;

    @GetMapping("/tests")
    @Operation(summary = "Tagastab andmebaasist kõik testid. competenceId on valikuline - kui see puudub, tagastatakse kõigi kompetentside testid.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "OK"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Kui kasutaja roll ei ole ADMIN ega HALDUR, siis 'message': Sul puudub õigus testi vaadata, 'errorCode': NO_PERMISSION_TO_VIEW_TEST",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public List<TestSummaryDto> findAllTestsBy(@RequestParam(required = false) Integer competenceId) {
        List<TestSummaryDto> testSummaryDtos = testService.findAllTestsBy(competenceId);
        return testSummaryDtos;
    }

    @GetMapping("/tests/{testId}")
    @Operation(summary = "Tagastab testi andmed koos küsimuste arvu ja maksimaalse punktisummaga (ilma küsimusteta).")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "OK"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Kasutaja ei ole sisse logitud"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Kui kasutaja roll ei ole ADMIN ega HALDUR, siis 'message': Sul puudub õigus testi vaadata, 'errorCode': NO_PERMISSION_TO_VIEW_TEST",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Kui testi ei leita, siis 'message': Ei leidnud primary keyd väärtusega: {testId}, 'errorCode': PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public TestDetailDto findTestDetail(@PathVariable Integer testId) {
        return testService.findTestDetail(testId);
    }

    @GetMapping("/tests/{testId}/start-info")
    @Operation(summary = "Tagastab andmebaasist konkreetse kasutajale määratud testi")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "OK"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Kui sisselogitud kasutajale ei ole seda testi määratud (või määramine/test/kasutaja pole aktiivne), siis 'message': Kasutajale ei ole vastavat testi määratud, 'errorCode': NO_TEST_ASSIGNMENT_FOR_THIS_USER"
            )
    })
    public TestStartDto findTestStartInfo(@PathVariable Integer testId) {
        return testService.findTestStartInfo(testId);
    }

    @PostMapping("/tests")
    @Operation(summary = "Lisab andmebaasi uue testi.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "OK"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslikud väljad pole täidetud, 'errorCode': INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Kui luba puudub, siis 'message': Sul puudub õigus testi luua, 'errorCode': NO_PERMISSION",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Kui andmebaasist vastet ei leita, siis 'message': Ei leidnud primary keyd väärtusega: {userId}, {competenceLevelId}, {questionId}, 'errorCode': PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )

    })
    public void createTest(@RequestBody @Valid TestCreateRequestDto testCreateRequestDto) {
        testService.createTest(testCreateRequestDto);

    }
}
