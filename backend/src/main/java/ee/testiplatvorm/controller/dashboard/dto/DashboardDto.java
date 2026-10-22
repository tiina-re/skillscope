package ee.testiplatvorm.controller.dashboard.dto;

import lombok.Data;

import java.util.List;

@Data
public class DashboardDto {
    private Long assignedTestCount;
    private Long recentTestCount;
    private Long recentQuestionCount;
    private Long pendingAiQuestionCount;
    private Integer recentDays;
    private List<CompetenceScoreDto> competenceScores;
    private Double averageScorePercent;
    private Long passedResultCount;
    private Long failedResultCount;
}
