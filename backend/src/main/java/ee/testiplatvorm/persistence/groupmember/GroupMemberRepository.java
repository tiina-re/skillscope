package ee.testiplatvorm.persistence.groupmember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Integer> {
    @Query("select gm from GroupMember gm join fetch gm.group")
    List<GroupMember> findAllGroupMembers();

    @Query("select gm from GroupMember gm where gm.user.id = :userId")
    List<GroupMember> findGroupMembersBy(Integer userId);

}
