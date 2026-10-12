package ee.testiplatvorm.persistence.aiquestion;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AiQuestionRepository extends JpaRepository<AiQuestion, Integer> {
    @Query("""
            select a from AiQuestion a
            where (:status is null or a.status = :status)
            and (:competenceId is null or a.competence.id = :competenceId)
            order by a.createdAt desc, a.id desc""")
    List<AiQuestion> findAiQuestionsBy(String status, Integer competenceId);

    @Query("select count(a) from AiQuestion a where a.status = :status")
    Long countAiQuestionsBy(String status);

    @Query("""
            select a from AiQuestion a
            where a.competenceLevel.id = :competenceLevelId and a.status = :status and a.score >= :minScore
            order by a.score desc, a.updatedAt desc""")
    List<AiQuestion> findGoodAiQuestionsBy(Integer competenceLevelId, String status, Integer minScore, Limit limit);

    @Query("""
            select a from AiQuestion a
            where a.competenceLevel.id = :competenceLevelId and a.status = :status and a.score <= :maxScore
            order by a.score asc, a.updatedAt desc""")
    List<AiQuestion> findBadAiQuestionsBy(Integer competenceLevelId, String status, Integer maxScore, Limit limit);
}