package com.example.bookstore.controller;


import com.example.bookstore.config.DaoAuthenticationProviderConfig;
import com.example.bookstore.search.service.BookSearchCustomService;
import com.example.bookstore.service.security.JpaUserDetailsService;
import com.example.bookstore.web.ApiExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import({ com.example.bookstore.controller.SecurityConfig.class, DaoAuthenticationProviderConfig.class, ApiExceptionHandler.class  })
class BookControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookSearchCustomService bookSearchService;

    @MockBean
    private JpaUserDetailsService userDetailsService; // used by DaoAuthenticationProviderConfig

    @MockBean
    private PasswordEncoder passwordEncoder;
    @BeforeEach
    void setupMock() {
        when(bookSearchService.searchBooks(
                any(),  any())
        ).thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));
        when(passwordEncoder.encode(anyString()))
                .thenAnswer(inv -> "{noop}" + inv.getArgument(0)); // or return some non-null hash
        when(bookSearchService.searchBooks(any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        Mockito.when(passwordEncoder.encode(Mockito.anyString()))
                .thenAnswer(inv -> "{noop}" + inv.getArgument(0));

        // Specific stubs
        Mockito.when(userDetailsService.loadUserByUsername("admin"))
                .thenReturn(User.withUsername("admin")
                        .password("{noop}admin123")
                        .roles("ADMIN", "USER")
                        .build());

        Mockito.when(userDetailsService.loadUserByUsername("user"))
                .thenReturn(User.withUsername("user")
                        .password("{noop}user123")
                        .roles("USER")
                        .build());
        Mockito.when(userDetailsService.loadUserByUsername("guest"))
                .thenReturn(User.withUsername("user")
                        .password("{noop}user123")
                        .roles("GUEST")
                        .build());


        // For any other username: throw UsernameNotFoundException
        Mockito.when(userDetailsService.loadUserByUsername(Mockito.argThat(u ->
                        !"admin".equals(u) && !"user".equals(u))))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // your service default
        Mockito.when(bookSearchService.searchBooks(Mockito.any(), Mockito.any()))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0,10), 0));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminAccess() throws Exception {
        mockMvc.perform(get("/api/v1/books")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldAllowUserAccess() throws Exception {
        mockMvc.perform(get("/api/v1/books")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminWithParams() throws Exception {
        mockMvc.perform(get("/api/v1/books").param("title", "Spring")).andExpect(status().isOk());
    }
    @Test
    @WithMockUser(roles = "USER")
    void shouldRejectPostMethod() throws Exception {
        mockMvc.perform(post("/api/v1/books"))
                .andExpect(status().isMethodNotAllowed()); // 405 expected
    }
    @Test
    @WithMockUser(roles = "USER")
    void shouldAllowUserWithAuthor() throws Exception {
        mockMvc.perform(get("/api/v1/books").param("author", "John")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminWithPagination() throws Exception {
        mockMvc.perform(get("/api/v1/books").param("page", "0").param("size", "5")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "GUEST")
    void shouldRejectGuestAccess() throws Exception {
        mockMvc.perform(get("/api/v1/books")).andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/books")).andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(roles = "USER")
    void shouldRejectInvalidMethod() throws Exception {
        mockMvc.perform(patch("/api/v1/books"))
                .andExpect(status().isMethodNotAllowed());
    }
}