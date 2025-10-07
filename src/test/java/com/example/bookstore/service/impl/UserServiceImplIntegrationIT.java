package com.example.bookstore.service.impl;


import com.example.bookstore.domain.Role;
import com.example.bookstore.domain.User;
import com.example.bookstore.dto.CreateUserRequest;
import com.example.bookstore.dto.UserResponse;
import com.example.bookstore.dto.UserStatusRequest;
import com.example.bookstore.exception.BookStoreException;
import com.example.bookstore.repository.RoleRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({UserServiceImpl.class, RoleServiceImpl.class, UserServiceImplIntegrationIT.TestBeans.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserServiceImplIntegrationIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @TestConfiguration
    static class TestBeans {
        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @Autowired private UserServiceImpl userService;
    @Autowired private RoleServiceImpl roleService;
    @Autowired private UserRepository userRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private RoleRepository roleRepository;

    @BeforeEach
    void cleanDbAndSeedRoles() {
        userRoleRepository.deleteAll();
        roleRepository.deleteAll();
        userRepository.deleteAll();

    }


    @Test
    void createUser_createsUser_encodesPassword_and_assignsRoles() {

        roleRepository.deleteAll();
        Role admin = new Role();
        admin.setName("ADMIN");
        roleRepository.saveAndFlush(admin);

        Role user = new Role();
        user.setName("USER");
        roleRepository.saveAndFlush(user);

        var req = new CreateUserRequest();

        req.setUsername("alice");
        req.setPassword("p@ss");
        req.setConfirmPassword("p@ss");
        req.setRoles(Set.of("ADMIN", "USER"));

        UserResponse dto = userService.createUser(req);

        assertThat(dto).isNotNull();
        assertThat(dto.getUsername()).isEqualTo("alice");

        // verify user persisted with encoded password
        Optional<User> saved = userRepository.findByUsernameExplicit("alice") == null
                ? Optional.empty()
                : Optional.ofNullable(userRepository.findByUsernameExplicit("alice"));
        assertThat(saved).isPresent();
        assertThat(saved.get().getPasswordHash()).isNotBlank();
        assertThat(saved.get().getPasswordHash()).isNotEqualTo("p@ss"); // encoded

        // verify role links saved (2 roles)
        assertThat(userRoleRepository.findAll())
                .hasSize(2)
                .allSatisfy(ur -> {
                    assertThat(ur.getUser().getUsername()).isEqualTo("alice");
                    assertThat(List.of("ADMIN", "USER")).contains(ur.getRole().getName());
                });
    }

    @Test
    void createUser_throws_whenUserAlreadyExists() {
        // pre-insert user
        User u = new User();
        u.setUsername("bob");
        u.setPasswordHash("x");
        userRepository.saveAndFlush(u);

        var req = new CreateUserRequest();
        req.setUsername("bob");
        req.setPassword("123");
        req.setConfirmPassword("123");
        req.setRoles(Set.of("USER"));

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(BookStoreException.class)
                // keep original message exactly as in service (including typos)
                .hasMessage("User allready exists with login=bob");
    }

    // ==========================================
    // createUser: password mismatch -> exception
    // ==========================================
    @Test
    void createUser_throws_whenPasswordsDoNotMatch() {
        var req = new CreateUserRequest();
        req.setUsername("carol");
        req.setPassword("one");
        req.setConfirmPassword("two");
        req.setRoles(Set.of("USER"));

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(BookStoreException.class)
                // original message (no space before username)
                .hasMessage("User passwords not unigue forcarol");
    }

    // ======================
    // activateUser: updates
    // ======================
    @Test
    void activateUser_setsEnabledTrue_andReasonDefault() {
        User u = new User();
        u.setUsername("dave");
        u.setEnabled(false);
        u.setPasswordHash("x");
        u = userRepository.saveAndFlush(u);

        var req = new UserStatusRequest(); // reason = null -> default used
        userService.activateUser(u.getId(), req);

        User reloaded = userRepository.findById(u.getId()).orElseThrow();
        assertThat(reloaded.isEnabled()).isTrue();
        assertThat(reloaded.getDescription()).isEqualTo("Activated by admin");
    }

    @Test
    void activateUser_setsCustomReason() {
        User u = new User();
        u.setUsername("erin");
        u.setEnabled(false);
        u.setPasswordHash("x");
        u = userRepository.saveAndFlush(u);

        var req = new UserStatusRequest();
        req.setReason("Manual activation by QA");
        userService.activateUser(u.getId(), req);

        User reloaded = userRepository.findById(u.getId()).orElseThrow();
        assertThat(reloaded.isEnabled()).isTrue();
        assertThat(reloaded.getDescription()).isEqualTo("Manual activation by QA");
    }

    // ========================
    // deactivateUser: updates
    // ========================
    @Test
    void deactivateUser_setsEnabledFalse_andReasonDefault() {
        User u = new User();
        u.setUsername("frank");
        u.setEnabled(true);
        u.setPasswordHash("x");
        u = userRepository.saveAndFlush(u);

        var req = new UserStatusRequest(); // null reason -> default used
        userService.deactivateUser(u.getId(), req);

        User reloaded = userRepository.findById(u.getId()).orElseThrow();
        assertThat(reloaded.isEnabled()).isFalse();
        assertThat(reloaded.getDescription()).isEqualTo("Deactivated by admin");
    }

    @Test
    void deactivateUser_setsCustomReason() {
        User u = new User();
        u.setUsername("grace");
        u.setEnabled(true);
        u.setPasswordHash("x");
        u = userRepository.saveAndFlush(u);

        var req = new UserStatusRequest();
        req.setReason("Policy violation");
        userService.deactivateUser(u.getId(), req);

        User reloaded = userRepository.findById(u.getId()).orElseThrow();
        assertThat(reloaded.isEnabled()).isFalse();
        assertThat(reloaded.getDescription()).isEqualTo("Policy violation");
    }

    // ===========================
    // getUserByName / findByUser*
    // ===========================
    @Test
    void getUserByName_returnsDto_whenExists() {
        User u = new User();
        u.setUsername("heidi");
        u.setPasswordHash("x");
        userRepository.saveAndFlush(u);

        UserResponse dto = userService.getUserByName("heidi");

        assertThat(dto).isNotNull();
        assertThat(dto.getUsername()).isEqualTo("heidi");
    }

    @Test
    void getUserByName_throws_whenMissing() {
        assertThatThrownBy(() -> userService.getUserByName("ghost"))
                .isInstanceOf(BookStoreException.class)
                .hasMessage("User with login not found=ghost");
    }

    @Test
    void findByUserId_throws_whenMissing() {
        assertThatThrownBy(() -> userService.findByUserId(999L))
                .isInstanceOf(BookStoreException.class)
                .hasMessage("User by id not found: 999");
    }
}
