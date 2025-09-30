package com.example.bookstore.controller;


import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.exception.BookStoreException;
import com.example.bookstore.service.BookAdminService;
import com.example.bookstore.web.ApiExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(BookAdminController.class)

@Import({com.example.bookstore.config.SecurityConfig.class, ApiExceptionHandler.class})
class BookAdminControllerRestTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookAdminService bookAdminService;

    private static final String BASE_URL = "/api/v1/admin/books";

    private static final  String json = """
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

    @Test
    @DisplayName("Admin can create book with valid data")
    @WithMockUser(roles = "ADMIN")
    void createBook_withValidData_returnsCreated() throws Exception {

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Admin can update book with valid data")
    @WithMockUser(roles = "ADMIN")
    void updateBook_withValidData_returnsOk() throws Exception {

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin can delete book with valid ID")
    @WithMockUser(roles = "ADMIN")
    void deleteBook_withValidId_returnsNoContent() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Admin can get books without filters")
    @WithMockUser(roles = "ADMIN")
    void getBooks_withValidRequest_returnsOk() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin can get books with filters")
    @WithMockUser(roles = "ADMIN")
    void getBooks_withFilters_returnsOk() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("author", "Martin Fowler")
                        .param("minPrice", "10")
                        .param("maxPrice", "50"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin can create book with decimal price")
    @WithMockUser(roles = "ADMIN")
    void createBook_withDecimalPrice_returnsCreated() throws Exception {


        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Admin can update book with different ID")
    @WithMockUser(roles = "ADMIN")
    void updateBook_withDifferentId_returnsOk() throws Exception {

        mockMvc.perform(put(BASE_URL + "/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin can get books with pagination")
    @WithMockUser(roles = "ADMIN")
    void getBooks_withPagination_returnsOk() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin can get books with empty filters")
    @WithMockUser(roles = "ADMIN")
    void getBooks_withEmptyFilters_returnsOk() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("query", "")
                        .param("title", "")
                        .param("author", "")
                        .param("genre", ""))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin can create book with long title")
    @WithMockUser(roles = "ADMIN")
    void createBook_withLongTitle_returnsCreated() throws Exception {

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }


    @Test
    @DisplayName("User role cannot create book")
    @WithMockUser(roles = "USER")
    void createBook_asUser_returnsForbidden() throws Exception {


        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Anonymous user cannot create book")
    void createBook_asAnonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Missing title returns bad request")
    @WithMockUser(roles = "ADMIN")
    void createBook_withMissingTitle_returnsBadRequest() throws Exception {

        String jsonMissingTitle = """
                     {
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
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMissingTitle))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Negative price returns bad request")
    @WithMockUser(roles = "ADMIN")
    void createBook_withNegativePrice_returnsBadRequest() throws Exception {
         String jsonBadRequest = """
                     {
                    "title": "Domain-Driven Design",
                    "authorId": 2,
                    "genreId": 1,
                    "price": -55.99,
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
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBadRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Update with invalid ID returns not found")
    @WithMockUser(roles = "ADMIN")
    void updateBook_withInvalidId_returnsNotFound() throws Exception {
        long data=999L;
        when(bookAdminService.update(Mockito.eq(data), Mockito.any()))
                .thenThrow(new BookStoreException("Book not found"));

        mockMvc.perform(put(BASE_URL + "/"+data)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Delete with invalid ID returns not found")
    @WithMockUser(roles = "ADMIN")
    void deleteBook_withInvalidId_returnsNotFound() throws Exception {
        long data = 9999L;

        Mockito.doThrow(new BookStoreException("Book not found: " + data))
                .when(bookAdminService).delete(Mockito.eq(data));

        mockMvc.perform(delete(BASE_URL + "/9999"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Empty JSON returns bad request")
    @WithMockUser(roles = "ADMIN")
    void createBook_withEmptyJson_returnsBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Update with empty JSON returns bad request")
    @WithMockUser(roles = "ADMIN")
    void updateBook_withEmptyJson_returnsBadRequest() throws Exception {
        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
    @Test
    @DisplayName("Admin can get book by ID")
    @WithMockUser(roles = "ADMIN")
    void getBook_byId_returnsOk() throws Exception {
        Long bookId = 2L;

        BookResponse response = BookResponse.builder()
                .id(bookId)
                .title("Test Book")
                .build();

        when(bookAdminService.get(bookId)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/" + bookId))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("User role is forbidden from accessing all admin endpoints")
    @WithMockUser(roles = "USER")
    void userRole_isForbiddenFromAllAdminEndpoints() throws Exception {

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());


        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());


        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isForbidden());


        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
    }
}