package com.example.bookstore.service.impl;

import com.example.bookstore.domain.Role;
import com.example.bookstore.exception.BookStoreException;
import com.example.bookstore.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RoleServiceImplTest {

    private RoleRepository roleRepository;
    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        roleRepository = mock(RoleRepository.class);
        roleService = new RoleServiceImpl(roleRepository);
    }

    // ✅ Positive cases

    @Test
    void shouldReturnRoleWhenFound() {
        Role role = new Role();
        role.setId(1L);
        role.setName("ADMIN");

        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(role));

        Role result = roleService.findByName("ADMIN");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("ADMIN");
    }

    @Test
    void shouldCallRepositoryOnce() {
        Role role = new Role();
        role.setId(2L);
        role.setName("USER");

        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));

        roleService.findByName("USER");

        verify(roleRepository, times(1)).findByName("USER");
        verifyNoMoreInteractions(roleRepository);
    }

    @Test
    void shouldPreserveRoleFields() {
        Role role = new Role();
        role.setId(3L);
        role.setName("MODERATOR");

        when(roleRepository.findByName("MODERATOR")).thenReturn(Optional.of(role));

        Role result = roleService.findByName("MODERATOR");

        assertThat(result.getName()).isEqualTo("MODERATOR");
        assertThat(result.getId()).isEqualTo(3L);
    }

    @Test
    void shouldReturnDifferentRoleByName() {
        Role role = new Role();
        role.setId(4L);
        role.setName("AUDITOR");

        when(roleRepository.findByName("AUDITOR")).thenReturn(Optional.of(role));

        Role result = roleService.findByName("AUDITOR");

        assertThat(result.getName()).isEqualTo("AUDITOR");
    }

    // ❌ Negative cases

    @Test
    void shouldThrowExceptionWhenRoleNotFound() {
        when(roleRepository.findByName("UNKNOWN")).thenReturn(Optional.empty());

        BookStoreException ex = assertThrows(BookStoreException.class, () -> roleService.findByName("UNKNOWN"));
        assertThat(ex.getMessage()).isEqualTo("Unknown role: UNKNOWN");
    }

    @Test
    void shouldThrowExceptionForNullName() {
        when(roleRepository.findByName(null))
                .thenThrow(new IllegalArgumentException("Role name cannot be null"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> roleService.findByName(null));
        assertThat(ex.getMessage()).isEqualTo("Role name cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenRepositoryFails() {
        when(roleRepository.findByName("ADMIN"))
                .thenThrow(new RuntimeException("Database error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> roleService.findByName("ADMIN"));
        assertThat(ex.getMessage()).isEqualTo("Database error");
    }

    @Test
    void shouldNotReturnRoleWhenOptionalIsEmpty() {
        when(roleRepository.findByName("GHOST")).thenReturn(Optional.empty());

        assertThrows(BookStoreException.class, () -> roleService.findByName("GHOST"));
    }
}
