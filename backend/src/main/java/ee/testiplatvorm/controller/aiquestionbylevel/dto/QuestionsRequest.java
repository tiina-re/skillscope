package ee.testiplatvorm.controller.aiquestionbylevel.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionsRequest {
    @NotNull
    private Integer competenceLevelId;
    private Integer questionTypeId;
    @NotNull @Min(1) @Max(5)
    private Integer questionCount;
    @Size(max = 500)
    private String instructions;
}
