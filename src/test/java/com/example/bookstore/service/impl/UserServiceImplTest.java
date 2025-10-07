package com.example.bookstore.service.impl;

import com.example.bookstore.domain.Role;
import com.example.bookstore.domain.User;
import com.example.bookstore.domain.UserRole;
import com.example.bookstore.dto.CreateUserRequest;
import com.example.bookstore.dto.UserResponse;
import com.example.bookstore.dto.UserStatusRequest;
import com.example.bookstore.exception.BookStoreException;
import com.example.bookstore.mappers.UserMapper;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.repository.UserRoleRepository;
import com.example.bookstore.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository userRepository;
    private RoleService roleService;
    private UserRoleRepository userRoleRepository;
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleService = mock(RoleService.class);
        userRoleRepository = mock(UserRoleRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        userService = new UserServiceImpl(userRepository, roleService, userRoleRepository, passwordEncoder);
    }

    // =========================
    // createUser: positive case
    // =========================
    @Test
    void createUser_shouldCreateUserEncodePassword_andAssignRoles() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("alice");
        request.setPassword("p@ss");
        request.setConfirmPassword("p@ss");
        request.setRoles(Set.of("ADMIN", "USER"));

        when(userRepository.findByUsernameExplicit("alice")).thenReturn(null); // not existing
        when(passwordEncoder.encode("p@ss")).thenReturn("ENCODED");

        User mappedUser = new User();
        mappedUser.setUsername("alice");
        User savedUser = new User();
        savedUser.setId(10L);
        savedUser.setUsername("alice");
        savedUser.setPasswordHash("ENCODED");

        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ADMIN");

        Role userRole = new Role();
        userRole.setId(2L);
        userRole.setName("USER");

        UserResponse mappedResponse = new UserResponse();
        mappedResponse.setId(10L);
        mappedResponse.setUsername("alice");

        try (MockedStatic<UserMapper> userMapperMock = mockStatic(UserMapper.class)) {
            userMapperMock.when(() -> UserMapper.fromCreateRequest(request)).thenReturn(mappedUser);
            userMapperMock.when(() -> UserMapper.toDto(mappedUser)).thenReturn(mappedResponse);

            when(userRepository.save(mappedUser)).thenReturn(savedUser);
            when(roleService.findByName("ADMIN")).thenReturn(adminRole);
            when(roleService.findByName("USER")).thenReturn(userRole);

            // Act
            UserResponse result = userService.createUser(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getUsername()).isEqualTo("alice");

            // password encoded + set
            verify(passwordEncoder, times(1)).encode("p@ss");
            assertThat(mappedUser.getPasswordHash()).isEqualTo("ENCODED");

            // user persisted once
            verify(userRepository, times(1)).save(mappedUser);

            // roles resolved
            verify(roleService, times(1)).findByName("ADMIN");
            verify(roleService, times(1)).findByName("USER");

            // user-role links created twice with savedUser + each role
            ArgumentCaptor<UserRole> userRoleCaptor = ArgumentCaptor.forClass(UserRole.class);
            verify(userRoleRepository, times(2)).save(userRoleCaptor.capture());

            List<UserRole> savedLinks = userRoleCaptor.getAllValues();
            assertThat(savedLinks).hasSize(2);
            assertThat(savedLinks.get(0).getUser()).isEqualTo(savedUser);
            assertThat(savedLinks.get(1).getUser()).isEqualTo(savedUser);
            assertThat(List.of(savedLinks.get(0).getRole().getName(), savedLinks.get(1).getRole().getName()))
                    .containsExactlyInAnyOrder("ADMIN", "USER");
        }
    }

    // ===================================
    // createUser: negative - duplicate user
    // ===================================
    @Test
    void createUser_shouldThrow_whenUserAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("bob");
        request.setPassword("123");
        request.setConfirmPassword("123");
        request.setRoles(Set.of("USER"));

        User existingUser = new User();
        existingUser.setUsername("bob");

        when(userRepository.findByUsernameExplicit("bob")).thenReturn(existingUser);

        BookStoreException ex = assertThrows(BookStoreException.class, () -> userService.createUser(request));
        // preserves exact message from service (with typo as-is)
        assertThat(ex.getMessage()).isEqualTo("User allready exists with login=bob");

        verify(userRepository, never()).save(any());
        verify(userRoleRepository, never()).save(any());
    }

    // ===================================
    // createUser: negative - password mismatch
    // ===================================
    @Test
    void createUser_shouldThrow_whenPasswordsDoNotMatch() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("carol");
        request.setPassword("one");
        request.setConfirmPassword("two");
        request.setRoles(Set.of("USER"));

        when(userRepository.findByUsernameExplicit("carol")).thenReturn(null);

        BookStoreException ex = assertThrows(BookStoreException.class, () -> userService.createUser(request));
        // note the original message lacks a space before username — keep it exact
        assertThat(ex.getMessage()).isEqualTo("User passwords not unigue forcarol");

        verify(userRepository, never()).save(any());
        verify(userRoleRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    // ==========================
    // activateUser: positive case
    // ==========================
    @Test
    void activateUser_shouldEnableAndSetDescription_andPersist() {
        Long userId = 100L;
        UserStatusRequest statusRequest = new UserStatusRequest();
        statusRequest.setReason(null); // will use default "Activated by admin"

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEnabled(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.activateUser(userId, statusRequest);

        assertThat(existingUser.isEnabled()).isTrue();
        assertThat(existingUser.getDescription()).isEqualTo("Activated by admin");
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void activateUser_shouldUseCustomReason() {
        Long userId = 101L;
        UserStatusRequest statusRequest = new UserStatusRequest();
        statusRequest.setReason("Manual activation by QA");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEnabled(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.activateUser(userId, statusRequest);

        assertThat(existingUser.isEnabled()).isTrue();
        assertThat(existingUser.getDescription()).isEqualTo("Manual activation by QA");
    }

    // ============================
    // deactivateUser: positive case
    // ============================
    @Test
    void deactivateUser_shouldDisableAndSetDescription_andPersist() {
        Long userId = 200L;
        UserStatusRequest statusRequest = new UserStatusRequest(); // no reason -> default used

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEnabled(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.deactivateUser(userId, statusRequest);

        assertThat(existingUser.isEnabled()).isFalse();
        assertThat(existingUser.getDescription()).isEqualTo("Deactivated by admin");
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void deactivateUser_shouldUseCustomReason() {
        Long userId = 201L;
        UserStatusRequest statusRequest = new UserStatusRequest();
        statusRequest.setReason("Policy violation");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEnabled(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.deactivateUser(userId, statusRequest);

        assertThat(existingUser.isEnabled()).isFalse();
        assertThat(existingUser.getDescription()).isEqualTo("Policy violation");
    }

    // =================================
    // getUserByName / findByUserName
    // =================================
    @Test
    void getUserByName_shouldReturnDto_whenUserExists() {
        String username = "dave";
        User foundUser = new User();
        foundUser.setId(300L);
        foundUser.setUsername(username);

        when(userRepository.findByUsernameExplicit(username)).thenReturn(foundUser);

        UserResponse dto = new UserResponse();
        dto.setId(300L);
        dto.setUsername(username);

        try (MockedStatic<UserMapper> userMapperMock = mockStatic(UserMapper.class)) {
            userMapperMock.when(() -> UserMapper.toDto(foundUser)).thenReturn(dto);

            UserResponse result = userService.getUserByName(username);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo(username);
            verify(userRepository, times(1)).findByUsernameExplicit(username);
        }
    }

    @Test
    void getUserByName_shouldThrow_whenUserMissing() {
        String username = "ghost";
        when(userRepository.findByUsernameExplicit(username)).thenReturn(null);

        BookStoreException ex = assertThrows(BookStoreException.class, () -> userService.getUserByName(username));
        assertThat(ex.getMessage()).isEqualTo("User with login not found=ghost");
    }

    @Test
    void findByUserName_shouldDelegateToRepository() {
        String username = "erin";
        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsernameExplicit(username)).thenReturn(user);

        User result = userService.findByUserName(username);

        assertThat(result).isEqualTo(user);
        verify(userRepository, times(1)).findByUsernameExplicit(username);
    }

    // ====================
    // findByUserId: errors
    // ====================
    @Test
    void findByUserId_shouldThrow_whenUserIdNotFound() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        BookStoreException ex = assertThrows(BookStoreException.class, () -> userService.findByUserId(userId));
        assertThat(ex.getMessage()).isEqualTo("User by id not found: 999");
    }
}
