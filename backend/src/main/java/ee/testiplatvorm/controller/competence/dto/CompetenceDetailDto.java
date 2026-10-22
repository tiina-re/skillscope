package ee.testiplatvorm.controller.competence.dto;

import lombok.Data;

@Data
public class CompetenceDetailDto {
    private Integer competenceId;
    private String competenceName;
    private String competenceShortDescription;
    private String competenceDescription;
}
