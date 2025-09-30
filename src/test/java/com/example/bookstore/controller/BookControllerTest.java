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

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import({com.example.bookstore.config.SecurityConfig.class, ApiExceptionHandler.class})
class BookControllerTest {

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
    void shouldAllowAdminAccessAndInvokeService() throws Exception {
        assertAccessAllowedForRole("ADMIN");
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldAllowUserAccessAndInvokeService() throws Exception {
        assertAccessAllowedForRole("USER");
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminWithParamsAndInvokeService() throws Exception {
        mockMvc.perform(get("/api/v1/books").param("title", "Spring"))
                .andExpect(status().isOk());

        verify(bookSearchService).searchBooks(
                Mockito.any(), Mockito.eq("Spring"), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldAllowUserWithAuthorAndInvokeService() throws Exception {
        mockMvc.perform(get("/api/v1/books").param("author", "John"))
                .andExpect(status().isOk());

        verify(bookSearchService).searchBooks(
                Mockito.any(), Mockito.any(), Mockito.eq("John"), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminWithPaginationAndInvokeService() throws Exception {
        mockMvc.perform(get("/api/v1/books").param("page", "0").param("size", "5"))
                .andExpect(status().isOk());

        verify(bookSearchService).searchBooks(
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    @WithMockUser(roles = "GUEST")
    void shouldRejectGuestAccess() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldRejectInvalidMethod() throws Exception {
        mockMvc.perform(patch("/api/v1/books"))
                .andExpect(status().isMethodNotAllowed());
    }

    private void assertAccessAllowedForRole(String role) throws Exception {
        mockMvc.perform(get("/api/v1/books")
                        .with(user("testUser").roles(role)))
                .andExpect(status().isOk());

        verify(bookSearchService).searchBooks(
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any());
    }

}