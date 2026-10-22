package ee.testiplatvorm.service;

import ee.testiplatvorm.controller.competence.dto.CompetenceDetailDto;
import ee.testiplatvorm.controller.competence.dto.CompetenceResponseDto;
import ee.testiplatvorm.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.testiplatvorm.persistence.competence.Competence;
import ee.testiplatvorm.persistence.competence.CompetenceMapper;
import ee.testiplatvorm.persistence.competence.CompetenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static ee.testiplatvorm.Status.STATUS_ACTIVE;

@Service
@RequiredArgsConstructor
public class CompetenceService {

    private final CompetenceRepository competenceRepository;
    private final CompetenceMapper competenceMapper;

    public List<CompetenceResponseDto> findCompetences() {
        List<Competence> competences = competenceRepository.findCompetencesBy(STATUS_ACTIVE.getCode());
        return competenceMapper.toCompetenceResponseDtos(competences);
    }

    public CompetenceDetailDto findCompetenceDetail(Integer competenceId) {
        Competence competence = getValidCompetenceBy(competenceId);
        return competenceMapper.toCompetenceDetailDto(competence);
    }

    public Competence getValidCompetenceBy(Integer competenceId) {
        return competenceRepository.findById(competenceId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("competenceId", competenceId));
    }
}
