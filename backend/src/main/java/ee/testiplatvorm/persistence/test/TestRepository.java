package ee.testiplatvorm.persistence.test;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;

public interface TestRepository extends JpaRepository<Test, Integer> {

    @Query("""
            select t from Test t
            where (:competenceId is null or t.competence.id = :competenceId)
            order by t.id""")
    List<Test> findAllTestsBy(Integer competenceId);

    @Query("select count(t) from Test t where t.createdAt >= :createdAfter")
    Long countTestsCreatedAfter(OffsetDateTime createdAfter);
}