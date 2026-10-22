package ee.testiplatvorm.persistence.result;

import ee.testiplatvorm.controller.dashboard.dto.CompetenceScoreDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Integer> {
    @Query("select r from Result r where r.userTest.id = :userTestId")
    Optional<Result> findResultByUserTestId(Integer userTestId);

    @Query("""
            select new ee.testiplatvorm.controller.dashboard.dto.CompetenceScoreDto(
                r.userTest.test.competence.name, avg(r.scoreTotal * 100.0 / r.maxScore))
            from Result r
            where r.completedAt is not null and r.maxScore > 0
            group by r.userTest.test.competence.name
            order by avg(r.scoreTotal * 100.0 / r.maxScore) desc""")
    List<CompetenceScoreDto> findCompetenceScores();

    @Query("select avg(r.scoreTotal * 100.0 / r.maxScore) from Result r where r.completedAt is not null and r.maxScore > 0")
    Double findAverageScorePercent();

    @Query("select count(r) from Result r where r.completedAt is not null and r.status = :status")
    Long countCompletedResultsBy(String status);
}