package ee.testiplatvorm.service;


import ee.testiplatvorm.Status;
import ee.testiplatvorm.controller.user.dto.NewUserRequest;
import ee.testiplatvorm.controller.user.dto.UserAddResponse;
import ee.testiplatvorm.controller.user.dto.UserResponse;
import ee.testiplatvorm.controller.user.dto.UserUpdateRequest;
import ee.testiplatvorm.infrastructure.exception.BadRequestException;
import ee.testiplatvorm.infrastructure.exception.DataNotFoundException;
import ee.testiplatvorm.infrastructure.exception.ForbiddenException;
import ee.testiplatvorm.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.testiplatvorm.persistence.Profile;
import ee.testiplatvorm.persistence.ProfileRepository;
import ee.testiplatvorm.persistence.Role;
import ee.testiplatvorm.persistence.group.Group;
import ee.testiplatvorm.persistence.groupmember.GroupMember;
import ee.testiplatvorm.persistence.groupmember.GroupMemberRepository;
import ee.testiplatvorm.persistence.user.User;
import ee.testiplatvorm.persistence.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static ee.testiplatvorm.Error.CANNOT_CHANGE_OWN_ROLE;
import static ee.testiplatvorm.Error.CANNOT_DELETE_YOURSELF;
import static ee.testiplatvorm.Error.EMAIL_ALREADY_EXISTS;
import static ee.testiplatvorm.Error.NO_PERMISSION_TO_MANAGE_GROUP;
import static ee.testiplatvorm.Error.NO_PERMISSION_TO_MANAGE_USERS;
import static ee.testiplatvorm.Error.PROFILE_NOT_FOUND;
import static ee.testiplatvorm.Error.USER_NOT_ACTIVE;
import static ee.testiplatvorm.Error.USER_NOT_DELETED;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ProfileRepository profileRepository;
    private final RoleService roleService;
    private final GroupService groupService;
    private final CurrentUserService currentUserService;

    public List<UserResponse> findAllUsers() {
        validateIsAdminOrManager(getCurrentUser());
        List<UserResponse> allUserResponses = userRepository.findAllUserResponses();
        List<GroupMember> groupMembers = groupMemberRepository.findAllGroupMembers();
        handleAddGroups(allUserResponses, groupMembers);
        return allUserResponses;
    }

    // HALDUR tohib lisada ainult KASUTAJA rolliga kasutajaid ja ainult oma hallatavatesse gruppidesse.
    // Kui e-post kuulub kustutatud kasutajale, taastatakse see kasutaja (testitulemused jäävad tema külge).
    @Transactional
    public UserAddResponse addUser(NewUserRequest newUserRequest) {
        User currentUser = getCurrentUser();
        validateIsAdminOrManager(currentUser);
        String email = newUserRequest.getEmail().trim().toLowerCase();
        Optional<User> existingUser = userRepository.findUserBy(email);
        validateEmailIsAvailable(existingUser);
        Role role = roleService.getValidRoleBy(newUserRequest.getRoleId());
        if (!isAdmin(currentUser)) {
            validateIsUserRole(role);
            List<Integer> managedGroupIds = groupService.findManagedGroupIds(currentUser.getId());
            for (Integer groupId : newUserRequest.getGroupIds()) {
                validateCanManageGroup(currentUser, managedGroupIds, groupId);
            }
        }
        OffsetDateTime now = OffsetDateTime.now();

        if (existingUser.isPresent()) {
            restoreUser(existingUser.get(), newUserRequest, role, now);
            return new UserAddResponse(true);
        }

        User user = createUser(email, newUserRequest.getPassword(), role, now);
        userRepository.save(user);

        Profile profile = createProfile(user, newUserRequest.getFirstName().trim(), newUserRequest.getLastName().trim(), now);
        profileRepository.save(profile);

        handleAddGroupMembers(user, newUserRequest.getGroupIds(), now);
        return new UserAddResponse(false);
    }

    // ADMIN muudab nime, rolli ja kõiki gruppe. HALDUR muudab ainult oma hallatavaid gruppe ning
    // enda ja oma gruppide liikmete nime (muul juhul jäetakse nimi vahele, roll jäetakse alati vahele).
    @Transactional
    public void updateUser(Integer userId, UserUpdateRequest userUpdateRequest) {
        User currentUser = getCurrentUser();
        validateIsAdminOrManager(currentUser);
        User user = getValidUserBy(userId);
        validateUserIsActive(user);
        OffsetDateTime now = OffsetDateTime.now();
        List<Integer> managedGroupIds = groupService.findManagedGroupIds(currentUser.getId());
        List<GroupMember> groupMembers = groupMemberRepository.findGroupMembersBy(user.getId());

        if (isAdmin(currentUser)) {
            updateRole(currentUser, user, userUpdateRequest.getRoleId(), now);
        } else {
            validateIsNotAdmin(user);
        }
        if (isAllowedToEditName(currentUser, user, managedGroupIds, groupMembers)) {
            updateName(user, userUpdateRequest, now);
        }
        updateGroupMembers(currentUser, user, managedGroupIds, groupMembers, userUpdateRequest.getGroupIds(), now);
    }

    public void deleteUser(Integer userId) {
        User currentUser = getCurrentUser();
        validateIsAdmin(currentUser);
        validateIsNotSelf(currentUser, userId);
        User user = getValidUserBy(userId);
        String code = Status.STATUS_INACTIVE.getCode();
        user.setStatus(code);
        OffsetDateTime now = OffsetDateTime.now();
        user.setUpdatedAt(now);
        userRepository.save(user);
    }

    // Kasutaja saab tagasi vana parooli, rolli ja grupid (kustutamine neid ei eemaldanud).
    // Registreerimata kasutaja (profiilita) läheb tagasi ootele, mitte aktiivseks.
    public void activateUser(Integer userId) {
        validateIsAdmin(getCurrentUser());
        User user = getValidUserBy(userId);
        if (!user.getStatus().equals(Status.STATUS_INACTIVE.getCode())) {
            throw new BadRequestException(USER_NOT_DELETED.getMessage(), USER_NOT_DELETED.name());
        }
        boolean hasProfile = profileRepository.findProfileBy(userId).isPresent();
        user.setStatus(hasProfile ? Status.STATUS_ACTIVE.getCode() : Status.STATUS_PENDING.getCode());
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
    }

    public User getValidUserBy(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new PrimaryKeyNotFoundException("userId", userId));
    }

    private User getCurrentUser() {
        return getValidUserBy(currentUserService.getUserId());
    }

    private boolean isAdmin(User user) {
        return user.getRole().getName().equals("ADMIN");
    }

    private void validateIsAdmin(User user) {
        if (!isAdmin(user)) {
            throw new ForbiddenException(NO_PERMISSION_TO_MANAGE_USERS.getMessage(), NO_PERMISSION_TO_MANAGE_USERS.name());
        }
    }

    private void validateIsNotAdmin(User user) {
        if (isAdmin(user)) {
            throw new ForbiddenException(NO_PERMISSION_TO_MANAGE_USERS.getMessage(), NO_PERMISSION_TO_MANAGE_USERS.name());
        }
    }

    private void validateIsAdminOrManager(User user) {
        if (!isAdmin(user) && !user.getRole().getName().equals("HALDUR")) {
            throw new ForbiddenException(NO_PERMISSION_TO_MANAGE_USERS.getMessage(), NO_PERMISSION_TO_MANAGE_USERS.name());
        }
    }

    private void validateIsNotSelf(User currentUser, Integer userId) {
        if (currentUser.getId().equals(userId)) {
            throw new ForbiddenException(CANNOT_DELETE_YOURSELF.getMessage(), CANNOT_DELETE_YOURSELF.name());
        }
    }

    private void validateUserIsActive(User user) {
        if (!user.getStatus().equals(Status.STATUS_ACTIVE.getCode())) {
            throw new BadRequestException(USER_NOT_ACTIVE.getMessage(), USER_NOT_ACTIVE.name());
        }
    }

    // Kustutatud kasutaja e-post on vaba, sest see kasutaja taastatakse
    private void validateEmailIsAvailable(Optional<User> existingUser) {
        boolean isTakenByNotDeletedUser = existingUser.isPresent()
                && !existingUser.get().getStatus().equals(Status.STATUS_INACTIVE.getCode());
        if (isTakenByNotDeletedUser) {
            throw new ForbiddenException(EMAIL_ALREADY_EXISTS.getMessage(), EMAIL_ALREADY_EXISTS.name());
        }
    }

    // Vanad grupid asendatakse vormis valitutega, profiil luuakse, kui seda polnud
    private void restoreUser(User user, NewUserRequest newUserRequest, Role role, OffsetDateTime now) {
        user.setPasswordHash(newUserRequest.getPassword());
        user.setRole(role);
        user.setStatus(Status.STATUS_ACTIVE.getCode());
        user.setUpdatedAt(now);
        userRepository.save(user);

        String firstName = newUserRequest.getFirstName().trim();
        String lastName = newUserRequest.getLastName().trim();
        Profile profile = profileRepository.findProfileBy(user.getId())
                .orElseGet(() -> createProfile(user, firstName, lastName, now));
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setUpdatedAt(now);
        profileRepository.save(profile);

        groupMemberRepository.deleteAll(groupMemberRepository.findGroupMembersBy(user.getId()));
        handleAddGroupMembers(user, newUserRequest.getGroupIds(), now);
    }

    private void validateIsUserRole(Role role) {
        if (!role.getName().equals("KASUTAJA")) {
            throw new ForbiddenException(NO_PERMISSION_TO_MANAGE_USERS.getMessage(), NO_PERMISSION_TO_MANAGE_USERS.name());
        }
    }

    private boolean isAllowedToEditName(User currentUser, User user, List<Integer> managedGroupIds, List<GroupMember> groupMembers) {
        if (isAdmin(currentUser) || currentUser.getId().equals(user.getId())) {
            return true;
        }
        return groupMembers.stream().anyMatch(groupMember -> managedGroupIds.contains(groupMember.getGroup().getId()));
    }

    private void updateRole(User currentUser, User user, Integer roleId, OffsetDateTime now) {
        boolean isRoleChanged = !user.getRole().getId().equals(roleId);
        if (user.getId().equals(currentUser.getId()) && isRoleChanged) {
            throw new ForbiddenException(CANNOT_CHANGE_OWN_ROLE.getMessage(), CANNOT_CHANGE_OWN_ROLE.name());
        }
        user.setRole(roleService.getValidRoleBy(roleId));
        user.setUpdatedAt(now);
        userRepository.save(user);
    }

    private void updateName(User user, UserUpdateRequest userUpdateRequest, OffsetDateTime now) {
        Profile profile = profileRepository.findProfileBy(user.getId())
                .orElseThrow(() -> new DataNotFoundException(PROFILE_NOT_FOUND.getMessage(), PROFILE_NOT_FOUND.name()));
        profile.setFirstName(userUpdateRequest.getFirstName().trim());
        profile.setLastName(userUpdateRequest.getLastName().trim());
        profile.setUpdatedAt(now);
        profileRepository.save(profile);
    }

    // Eemaldab grupid, mida päringus enam pole, ja lisab uued. HALDUR tohib puudutada ainult oma hallatavaid gruppe.
    private void updateGroupMembers(User currentUser, User user, List<Integer> managedGroupIds, List<GroupMember> groupMembers, List<Integer> groupIds, OffsetDateTime now) {
        List<Integer> newGroupIds = groupIds.stream().distinct().toList();

        for (GroupMember groupMember : groupMembers) {
            Integer groupId = groupMember.getGroup().getId();
            if (!newGroupIds.contains(groupId)) {
                validateCanManageGroup(currentUser, managedGroupIds, groupId);
                groupMemberRepository.delete(groupMember);
            }
        }

        List<Integer> currentGroupIds = groupMembers.stream().map(groupMember -> groupMember.getGroup().getId()).toList();
        for (Integer groupId : newGroupIds) {
            if (!currentGroupIds.contains(groupId)) {
                validateCanManageGroup(currentUser, managedGroupIds, groupId);
                Group group = groupService.getValidGroupBy(groupId);
                groupMemberRepository.save(createGroupMember(group, user, currentUser, now));
            }
        }
    }

    private void validateCanManageGroup(User currentUser, List<Integer> managedGroupIds, Integer groupId) {
        if (!isAdmin(currentUser) && !managedGroupIds.contains(groupId)) {
            throw new ForbiddenException(NO_PERMISSION_TO_MANAGE_GROUP.getMessage(), NO_PERMISSION_TO_MANAGE_GROUP.name());
        }
    }

    private User createUser(String email, String password, Role role, OffsetDateTime now) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(password);
        user.setRole(role);
        user.setStatus(Status.STATUS_ACTIVE.getCode());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return user;
    }

    private Profile createProfile(User user, String firstName, String lastName, OffsetDateTime now) {
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
        return profile;
    }

    private void handleAddGroupMembers(User user, List<Integer> groupIds, OffsetDateTime now) {
        User addedBy = getCurrentUser();
        for (Integer groupId : groupIds.stream().distinct().toList()) {
            Group group = groupService.getValidGroupBy(groupId);
            GroupMember groupMember = createGroupMember(group, user, addedBy, now);
            groupMemberRepository.save(groupMember);
        }
    }

    private GroupMember createGroupMember(Group group, User user, User addedBy, OffsetDateTime now) {
        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(group);
        groupMember.setUser(user);
        groupMember.setAddedBy(addedBy);
        groupMember.setCreatedAt(now);
        groupMember.setUpdatedAt(now);
        return groupMember;
    }

    private void handleAddGroups(List<UserResponse> allUserResponses, List<GroupMember> groupMembers) {
        for (UserResponse userResponse : allUserResponses) {
            for (GroupMember groupMember : groupMembers) {
                if (userResponse.getUserId().equals(groupMember.getUser().getId())) {
                    userResponse.getGroupIds().add(groupMember.getGroup().getId());
                    userResponse.getGroupNames().add(groupMember.getGroup().getName());
                }
            }

        }
    }
}
