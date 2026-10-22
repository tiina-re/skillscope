package ee.testiplatvorm.controller.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompetenceScoreDto {
    private String competenceName;
    private Double averageScorePercent;
}
