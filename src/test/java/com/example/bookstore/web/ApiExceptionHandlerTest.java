package com.example.bookstore.web;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    @DisplayName("Validation: joins field errors; null defaultMessage -> 'invalid'")
    void handleValidationException_buildsMessageAndReturns400() {
        HttpServletRequest req = mockReq("/api/v1/books");
        BindingResult br = mock(BindingResult.class);

        FieldError e1 = new FieldError("book", "title", "must not be blank");
        FieldError e2 = new FieldError("book", "price", null); // should fallback to 'invalid'
        when(br.getFieldErrors()).thenReturn(List.of(e1, e2));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(br);

        ResponseEntity<ApiError> resp = handler.handleValidationException(ex, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiError body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(body.getError()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
        assertThat(body.getPath()).isEqualTo("/api/v1/books");
        assertThat(body.getMessage())
                .isEqualTo("title: must not be blank, price: invalid");
    }

    @Test
    @DisplayName("EntityNotFound -> 404 with exception message")
    void handleEntityNotFound_returns404() {
        HttpServletRequest req = mockReq("/api/v1/books/999");
        EntityNotFoundException ex = new EntityNotFoundException("Book not found");

        ResponseEntity<ApiError> resp = handler.handleEntityNotFoundException(ex, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ApiError body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(404);
        assertThat(body.getError()).isEqualTo("Not Found");
        assertThat(body.getMessage()).isEqualTo("Book not found");
        assertThat(body.getPath()).isEqualTo("/api/v1/books/999");
    }

    @Test
    @DisplayName("AccessDenied -> 403 with fixed message")
    void handleAccessDenied_returns403() {
        HttpServletRequest req = mockReq("/api/v1/admin/books");
        AccessDeniedException ex = new AccessDeniedException("forbidden");

        ResponseEntity<ApiError> resp = handler.handleAccessDenied(ex, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        ApiError body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(403);
        assertThat(body.getError()).isEqualTo("Forbidden");
        assertThat(body.getMessage()).isEqualTo("Access Denied");
        assertThat(body.getPath()).isEqualTo("/api/v1/admin/books");
    }

    @Test
    @DisplayName("Unhandled exception -> 500 with exception message")
    void handleUnhandledException_returns500() {
        HttpServletRequest req = mockReq("/boom");
        Exception ex = new RuntimeException("kaboom");

        ResponseEntity<ApiError> resp = handler.handleUnhandledException(ex, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ApiError body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(500);
        assertThat(body.getError()).isEqualTo("Internal Server Error");
        assertThat(body.getMessage()).isEqualTo("kaboom");
        assertThat(body.getPath()).isEqualTo("/boom");
    }

    @Test
    @DisplayName("NoHandlerFound -> 404 'Endpoint not found'")
    void handleNoHandlerFound_returns404()  {
        HttpServletRequest req = mockReq("/nope");
        NoHandlerFoundException ex =
                new NoHandlerFoundException("GET", "/nope", new HttpHeaders());

        ResponseEntity<ApiError> resp = handler.handleNoHandlerFound(ex, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ApiError body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(404);
        assertThat(body.getError()).isEqualTo("Not Found");
        assertThat(body.getMessage()).isEqualTo("Endpoint not found");
        assertThat(body.getPath()).isEqualTo("/nope");
    }

    @Test
    @DisplayName("MethodNotSupported -> 405 'HTTP method not supported'")
    void handleMethodNotSupported_returns405() {
        HttpServletRequest req = mockReq("/api/v1/books");
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("PATCH");

        ResponseEntity<ApiError> resp = handler.handleMethodNotSupported(ex, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        ApiError body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(405);
        assertThat(body.getError()).isEqualTo("Method Not Allowed");
        assertThat(body.getMessage()).isEqualTo("HTTP method not supported");
        assertThat(body.getPath()).isEqualTo("/api/v1/books");
    }

    // helpers
    private static HttpServletRequest mockReq(String uri) {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn(uri);
        return req;
    }
}
