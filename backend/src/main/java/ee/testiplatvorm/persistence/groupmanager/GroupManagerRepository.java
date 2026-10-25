package ee.testiplatvorm.persistence.groupmanager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GroupManagerRepository extends JpaRepository<GroupManager, Integer> {
    @Query("select gm.group.id from GroupManager gm where gm.user.id = :userId and gm.status = :status")
    List<Integer> findManagedGroupIdsBy(Integer userId, String status);
}
