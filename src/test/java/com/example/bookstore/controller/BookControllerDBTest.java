package com.example.bookstore.controller;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bookstore.config.DaoAuthenticationProviderConfig;
import com.example.bookstore.dto.BookSearchDto;
import com.example.bookstore.search.service.BookSearchCustomService;
import com.example.bookstore.service.security.JpaUserDetailsService;
import com.example.bookstore.web.ApiExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

@WebMvcTest(BookController.class)

@Import({ SecurityConfig.class, DaoAuthenticationProviderConfig.class, ApiExceptionHandler.class  })
class BookControllerDBTest {
    private static final String URL_SEARCH = "/api/v1/books/db";
    private static final String JSON_PAYLOAD = """
                     {
                    "title": "Domain-Driven Design",
                    "authorId": 2,
                    "genreId": 1,
                    "price": 55.99,
                    "caption": "Blue hardcover",
                    "description": "Evans classic on DDD",
                    "isbn": "111-111111224",
                    "publishedYear": 2003,
                    "publisher": "Addison-Wesley",
                    "pageCount": 560,
                    "language": "en",
                    "stock": 10,
                    "coverImageUrl": "https://example.com/ddd.jpg"
                    }
                """;
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookSearchCustomService bookSearchService;

   @MockBean
    private JpaUserDetailsService userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;
    @BeforeEach
    void setupMock() {
        when(bookSearchService.searchDb(
                any(),  any())
        ).thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));
        when(passwordEncoder.encode(anyString()))
                .thenAnswer(inv -> "{noop}" + inv.getArgument(0)); // or return some non-null hash
        when(bookSearchService.searchDb(any(), any()))
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


        Mockito.when(userDetailsService.loadUserByUsername(Mockito.argThat(u ->
                        !"admin".equals(u) && !"user".equals(u))))
                .thenThrow(new UsernameNotFoundException("User not found"));

        Mockito.when(bookSearchService.searchDb(Mockito.any(), Mockito.any()))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0,10), 0));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN","USER"})
    void adminCanCall_andPassesTitleFilter() throws Exception {
        when(bookSearchService.searchDb(any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get(URL_SEARCH)
                        .param("title", "Spring")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<BookSearchDto> captor = ArgumentCaptor.forClass(BookSearchDto.class);
        verify(bookSearchService).searchDb(captor.capture(), any(Pageable.class));
        assertEquals("Spring", captor.getValue().getTitle());
    }


    @Test
    @WithMockUser(roles = "USER")
    void shouldAllowUserAccessAndInvokeService() throws Exception {
        assertAccessAllowedForRole("USER");
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminWithParamsAndInvokeService() throws Exception {
        mockMvc.perform(get(URL_SEARCH).param("title", "Spring"))
                .andExpect(status().isOk());

        ArgumentCaptor<BookSearchDto> captor = ArgumentCaptor.forClass(BookSearchDto.class);
        verify(bookSearchService).searchDb(captor.capture(), any(Pageable.class));
        assertEquals("Spring", captor.getValue().getTitle());

    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldAllowUserWithAuthorAndInvokeService() throws Exception {
        mockMvc.perform(get(URL_SEARCH).param("author", "John"))
                .andExpect(status().isOk());

        ArgumentCaptor<BookSearchDto> captor = ArgumentCaptor.forClass(BookSearchDto.class);
        verify(bookSearchService).searchDb(captor.capture(), any(Pageable.class));
        assertEquals("John", captor.getValue().getAuthor());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminWithPaginationAndInvokeService() throws Exception {
        mockMvc.perform(get(URL_SEARCH).param("page", "0").param("size", "5"))
                .andExpect(status().isOk());

        verify(bookSearchService).searchDb( any(), any() );
    }

    @Test
    @WithMockUser(roles = "GUEST")
    void shouldRejectGuestAccess() throws Exception {
        mockMvc.perform(get(URL_SEARCH))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get(URL_SEARCH))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldRejectInvalidMethod() throws Exception {
        mockMvc.perform(patch(URL_SEARCH))
                .andExpect(status().isMethodNotAllowed());
    }

    private void assertAccessAllowedForRole(String role) throws Exception {
        mockMvc.perform(get(URL_SEARCH)
                        .with(user("testUser").roles(role)))
                .andExpect(status().isOk());

        verify(bookSearchService).searchDb(  any(), any() );
    }
    @Test
    @DisplayName("Admin can get books with filters")
    @WithMockUser(roles = "ADMIN")
    void getBooks_withFilters_returnsOk() throws Exception {
        mockMvc.perform(get(URL_SEARCH)
                        .param("author", "Martin Fowler")
                        .param("minPrice", "10")
                        .param("maxPrice", "50"))
                .andExpect(status().isOk());
    }

}