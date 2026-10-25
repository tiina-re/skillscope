package ee.testiplatvorm.service;

import ee.testiplatvorm.Status;
import ee.testiplatvorm.controller.user.dto.NewUserRequest;
import ee.testiplatvorm.controller.user.dto.UserAddResponse;
import ee.testiplatvorm.controller.user.dto.UserUpdateRequest;
import ee.testiplatvorm.infrastructure.exception.ForbiddenException;
import ee.testiplatvorm.persistence.Profile;
import ee.testiplatvorm.persistence.ProfileRepository;
import ee.testiplatvorm.persistence.Role;
import ee.testiplatvorm.persistence.group.Group;
import ee.testiplatvorm.persistence.groupmember.GroupMember;
import ee.testiplatvorm.persistence.groupmember.GroupMemberRepository;
import ee.testiplatvorm.persistence.user.User;
import ee.testiplatvorm.persistence.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static ee.testiplatvorm.Error.CANNOT_CHANGE_OWN_ROLE;
import static ee.testiplatvorm.Error.CANNOT_DELETE_YOURSELF;
import static ee.testiplatvorm.Error.EMAIL_ALREADY_EXISTS;
import static ee.testiplatvorm.Error.NO_PERMISSION_TO_MANAGE_GROUP;
import static ee.testiplatvorm.Error.NO_PERMISSION_TO_MANAGE_USERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Kasutajate halduse õiguste reeglid (ADMIN vs HALDUR) ja kustutatud kasutaja taastamine.
// Rollide id-d: 1 = ADMIN, 2 = HALDUR, 3 = KASUTAJA. Haldur (id 2) haldab gruppi 10, kasutaja (id 5) on grupis 20.
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // Kõik UserService konstruktori sõltuvused on mockid
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupMemberRepository groupMemberRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private RoleService roleService;
    @Mock
    private GroupService groupService;
    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private UserService userService;

    @Test
    void deleteUser_adminDeletesHimself_throwsForbiddenException() {
        // Arrange
        User admin = createUser(1, "ADMIN", Status.STATUS_ACTIVE);
        loginAs(admin);

        // Act + Assert
        assertThatThrownBy(() -> userService.deleteUser(1))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(CANNOT_DELETE_YOURSELF.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_manager_throwsForbiddenException() {
        // Arrange
        User manager = createUser(2, "HALDUR", Status.STATUS_ACTIVE);
        loginAs(manager);

        // Act + Assert
        assertThatThrownBy(() -> userService.deleteUser(5))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(NO_PERMISSION_TO_MANAGE_USERS.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_adminDeletesOtherUser_setsStatusInactive() {
        // Arrange
        User admin = createUser(1, "ADMIN", Status.STATUS_ACTIVE);
        User user = createUser(5, "KASUTAJA", Status.STATUS_ACTIVE);
        loginAs(admin);
        when(userRepository.findById(5)).thenReturn(Optional.of(user));

        // Act
        userService.deleteUser(5);

        // Assert
        assertThat(user.getStatus()).isEqualTo(Status.STATUS_INACTIVE.getCode());
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_adminChangesOwnRole_throwsForbiddenException() {
        // Arrange
        User admin = createUser(1, "ADMIN", Status.STATUS_ACTIVE);
        loginAs(admin);
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest("Anna", "Admin", 3, List.of(10));

        // Act + Assert
        assertThatThrownBy(() -> userService.updateUser(1, userUpdateRequest))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(CANNOT_CHANGE_OWN_ROLE.getMessage());
        assertThat(admin.getRole().getName()).isEqualTo("ADMIN");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_managerEditsAdmin_throwsForbiddenException() {
        // Arrange
        User manager = createUser(2, "HALDUR", Status.STATUS_ACTIVE);
        User admin = createUser(1, "ADMIN", Status.STATUS_ACTIVE);
        loginAs(manager);
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest("Anna", "Admin", 1, List.of(10));

        // Act + Assert
        assertThatThrownBy(() -> userService.updateUser(1, userUpdateRequest))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(NO_PERMISSION_TO_MANAGE_USERS.getMessage());
        verify(profileRepository, never()).save(any());
        verify(groupMemberRepository, never()).save(any());
    }

    @Test
    void updateUser_managerEditsUserOutsideManagedGroups_skipsRoleAndNameButAddsManagedGroup() {
        // Arrange
        User manager = createUser(2, "HALDUR", Status.STATUS_ACTIVE);
        User user = createUser(5, "KASUTAJA", Status.STATUS_ACTIVE);
        loginAs(manager);
        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        when(groupService.findManagedGroupIds(2)).thenReturn(List.of(10));
        when(groupMemberRepository.findGroupMembersBy(5)).thenReturn(List.of(createGroupMember(20, user)));
        when(groupService.getValidGroupBy(10)).thenReturn(createGroup(10));
        // Roll 1 (ADMIN) ja uus nimi jäetakse vahele, grupp 20 jääb alles, grupp 10 lisatakse
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest("Uus", "Nimi", 1, List.of(20, 10));

        // Act
        userService.updateUser(5, userUpdateRequest);

        // Assert
        assertThat(user.getRole().getName()).isEqualTo("KASUTAJA");
        verify(userRepository, never()).save(any());
        verify(profileRepository, never()).save(any());
        verify(groupMemberRepository, never()).delete(any());
        ArgumentCaptor<GroupMember> groupMemberCaptor = ArgumentCaptor.forClass(GroupMember.class);
        verify(groupMemberRepository).save(groupMemberCaptor.capture());
        assertThat(groupMemberCaptor.getValue().getGroup().getId()).isEqualTo(10);
    }

    @Test
    void updateUser_managerRemovesUnmanagedGroup_throwsForbiddenException() {
        // Arrange
        User manager = createUser(2, "HALDUR", Status.STATUS_ACTIVE);
        User user = createUser(5, "KASUTAJA", Status.STATUS_ACTIVE);
        loginAs(manager);
        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        when(groupService.findManagedGroupIds(2)).thenReturn(List.of(10));
        when(groupMemberRepository.findGroupMembersBy(5)).thenReturn(List.of(createGroupMember(20, user)));
        // Grupp 20 eemaldataks, aga haldur seda gruppi ei halda
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest("Mari", "Maasikas", 3, List.of(10));

        // Act + Assert
        assertThatThrownBy(() -> userService.updateUser(5, userUpdateRequest))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(NO_PERMISSION_TO_MANAGE_GROUP.getMessage());
        verify(groupMemberRepository, never()).delete(any());
        verify(groupMemberRepository, never()).save(any());
    }

    @Test
    void addUser_emailTakenByActiveUser_throwsForbiddenException() {
        // Arrange
        User admin = createUser(1, "ADMIN", Status.STATUS_ACTIVE);
        User existingUser = createUser(5, "KASUTAJA", Status.STATUS_ACTIVE);
        loginAs(admin);
        when(userRepository.findUserBy("mari@test.ee")).thenReturn(Optional.of(existingUser));
        NewUserRequest newUserRequest = new NewUserRequest("mari@test.ee", "parool123", "Mari", "Maasikas", 3, List.of(10));

        // Act + Assert
        assertThatThrownBy(() -> userService.addUser(newUserRequest))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(EMAIL_ALREADY_EXISTS.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUser_managerAddsAdmin_throwsForbiddenException() {
        // Arrange
        User manager = createUser(2, "HALDUR", Status.STATUS_ACTIVE);
        loginAs(manager);
        when(userRepository.findUserBy("uus@test.ee")).thenReturn(Optional.empty());
        when(roleService.getValidRoleBy(1)).thenReturn(createRole(1, "ADMIN"));
        NewUserRequest newUserRequest = new NewUserRequest("uus@test.ee", "parool123", "Uus", "Admin", 1, List.of(10));

        // Act + Assert
        assertThatThrownBy(() -> userService.addUser(newUserRequest))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(NO_PERMISSION_TO_MANAGE_USERS.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUser_managerAddsUserToUnmanagedGroup_throwsForbiddenException() {
        // Arrange
        User manager = createUser(2, "HALDUR", Status.STATUS_ACTIVE);
        loginAs(manager);
        when(userRepository.findUserBy("uus@test.ee")).thenReturn(Optional.empty());
        when(roleService.getValidRoleBy(3)).thenReturn(createRole(3, "KASUTAJA"));
        when(groupService.findManagedGroupIds(2)).thenReturn(List.of(10));
        NewUserRequest newUserRequest = new NewUserRequest("uus@test.ee", "parool123", "Uus", "Kasutaja", 3, List.of(20));

        // Act + Assert
        assertThatThrownBy(() -> userService.addUser(newUserRequest))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(NO_PERMISSION_TO_MANAGE_GROUP.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUser_emailOfDeletedUser_restoresUserAndReplacesGroups() {
        // Arrange
        User admin = createUser(1, "ADMIN", Status.STATUS_ACTIVE);
        User deletedUser = createUser(5, "KASUTAJA", Status.STATUS_INACTIVE);
        loginAs(admin);
        // E-post normaliseeritakse enne otsingut (trim + väiketähed)
        when(userRepository.findUserBy("mari@test.ee")).thenReturn(Optional.of(deletedUser));
        when(roleService.getValidRoleBy(3)).thenReturn(createRole(3, "KASUTAJA"));
        when(profileRepository.findProfileBy(5)).thenReturn(Optional.empty());
        List<GroupMember> oldGroupMembers = List.of(createGroupMember(20, deletedUser));
        when(groupMemberRepository.findGroupMembersBy(5)).thenReturn(oldGroupMembers);
        when(groupService.getValidGroupBy(10)).thenReturn(createGroup(10));
        NewUserRequest newUserRequest = new NewUserRequest(" Mari@Test.ee ", "parool123", " Mari ", "Maasikas", 3, List.of(10));

        // Act
        UserAddResponse userAddResponse = userService.addUser(newUserRequest);

        // Assert
        assertThat(userAddResponse.getRestored()).isTrue();
        assertThat(deletedUser.getStatus()).isEqualTo(Status.STATUS_ACTIVE.getCode());
        verify(userRepository).save(deletedUser);

        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository).save(profileCaptor.capture());
        assertThat(profileCaptor.getValue().getFirstName()).isEqualTo("Mari");

        verify(groupMemberRepository).deleteAll(oldGroupMembers);
        ArgumentCaptor<GroupMember> groupMemberCaptor = ArgumentCaptor.forClass(GroupMember.class);
        verify(groupMemberRepository).save(groupMemberCaptor.capture());
        assertThat(groupMemberCaptor.getValue().getGroup().getId()).isEqualTo(10);
    }

    @Test
    void activateUser_deletedUserWithoutProfile_becomesPending() {
        // Arrange
        User admin = createUser(1, "ADMIN", Status.STATUS_ACTIVE);
        User user = createUser(5, "KASUTAJA", Status.STATUS_INACTIVE);
        loginAs(admin);
        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        when(profileRepository.findProfileBy(5)).thenReturn(Optional.empty());

        // Act
        userService.activateUser(5);

        // Assert
        assertThat(user.getStatus()).isEqualTo(Status.STATUS_PENDING.getCode());
        verify(userRepository).save(user);
    }

    private void loginAs(User user) {
        when(currentUserService.getUserId()).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    }

    private User createUser(Integer userId, String roleName, Status status) {
        Integer roleId = switch (roleName) {
            case "ADMIN" -> 1;
            case "HALDUR" -> 2;
            default -> 3;
        };
        User user = new User();
        user.setId(userId);
        user.setRole(createRole(roleId, roleName));
        user.setStatus(status.getCode());
        return user;
    }

    private Role createRole(Integer roleId, String name) {
        Role role = new Role();
        role.setId(roleId);
        role.setName(name);
        return role;
    }

    private Group createGroup(Integer groupId) {
        Group group = new Group();
        group.setId(groupId);
        return group;
    }

    private GroupMember createGroupMember(Integer groupId, User user) {
        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(createGroup(groupId));
        groupMember.setUser(user);
        return groupMember;
    }
}
