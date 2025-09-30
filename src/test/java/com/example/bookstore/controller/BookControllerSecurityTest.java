package com.example.bookstore.controller;


import com.example.bookstore.search.service.BookSearchCustomService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import({com.example.bookstore.config.SecurityConfig.class, ApiExceptionHandler.class})
class BookControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookSearchCustomService bookSearchService;

    @BeforeEach
    void setupMock() {
        Mockito.when(bookSearchService.searchBooks(
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any())
        ).thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));
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