package ee.testiplatvorm.service;

import ee.testiplatvorm.controller.group.dto.GroupResponse;
import ee.testiplatvorm.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.testiplatvorm.persistence.group.Group;
import ee.testiplatvorm.persistence.group.GroupRepository;
import ee.testiplatvorm.persistence.groupmanager.GroupManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static ee.testiplatvorm.Status.STATUS_ACTIVE;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupManagerRepository groupManagerRepository;
    private final CurrentUserService currentUserService;

    public List<GroupResponse> findActiveGroups() {
        return groupRepository.findGroupResponsesBy(STATUS_ACTIVE.getCode());
    }

    public List<Integer> findCurrentUserManagedGroupIds() {
        return findManagedGroupIds(currentUserService.getUserId());
    }

    public List<Integer> findManagedGroupIds(Integer userId) {
        return groupManagerRepository.findManagedGroupIdsBy(userId, STATUS_ACTIVE.getCode());
    }

    public Group getValidGroupBy(Integer groupId) {
        return groupRepository.findById(groupId).orElseThrow(() -> new PrimaryKeyNotFoundException("groupId", groupId));
    }
}
