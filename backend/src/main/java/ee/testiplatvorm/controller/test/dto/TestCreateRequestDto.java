package ee.testiplatvorm.controller.test.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class TestCreateRequestDto {
    @NotNull
    private Integer competenceId;
    @NotNull
    private Integer competenceLevelId;
    @NotBlank
    private String testName;
    @NotBlank
    private String testShortDescription;
    @NotBlank
    private String testDescription;
    @NotNull
    private Boolean isTimed;
    private Integer timerMin;
    @NotNull
    private Integer passPercent;
    @NotNull
    private Boolean roundScoreUp;
    @NotEmpty
    @Valid
    private List<TestQuestionDto> questions;
}
