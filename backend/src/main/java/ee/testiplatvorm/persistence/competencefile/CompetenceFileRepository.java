package ee.testiplatvorm.persistence.competencefile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CompetenceFileRepository extends JpaRepository<CompetenceFile, Integer> {
    @Query("select c from CompetenceFile c where c.competence.id = :competenceId order by c.id")
    List<CompetenceFile> findCompetenceFilesBy(Integer competenceId);
}
