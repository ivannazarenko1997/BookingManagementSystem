package com.example.bookstore.exception;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookStoreExceptionTest {

    @Test
    void shouldCreateExceptionWithNoArgs() {
        BookStoreException ex = new BookStoreException();
        assertThat(ex).isInstanceOf(BookStoreException.class);
        assertThat(ex.getMessage()).isNull();
    }

    @Test
    void shouldCreateExceptionWithMessage() {
        BookStoreException ex = new BookStoreException("Error occurred");
        assertThat(ex.getMessage()).isEqualTo("Error occurred");
    }

    @Test
    void shouldCreateExceptionWithCause() {
        Throwable cause = new RuntimeException("Cause");
        BookStoreException ex = new BookStoreException(cause);
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        Throwable cause = new RuntimeException("Cause");
        BookStoreException ex = new BookStoreException("Error", cause);
        assertThat(ex.getMessage()).isEqualTo("Error");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void shouldCreateExceptionWithAllArgs() {
        Throwable cause = new RuntimeException("Cause");
        BookStoreException ex = new BookStoreException("Error", cause, true, false);
        assertThat(ex.getMessage()).isEqualTo("Error");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void shouldThrowAndCatchException() {
        Exception thrown = assertThrows(BookStoreException.class, () -> {
            throw new BookStoreException("Boom");
        });
        assertThat(thrown.getMessage()).isEqualTo("Boom");
    }

    @Test
    void shouldPreserveStackTraceFlag() {
        Throwable cause = new RuntimeException("Cause");
        BookStoreException ex = new BookStoreException("Error", cause, true, false);
        assertThat(ex.getStackTrace()).isEmpty(); // writableStackTrace = false
    }

    @Test
    void shouldSupportSuppression() {
        Throwable cause = new RuntimeException("Cause");
        BookStoreException ex = new BookStoreException("Error", cause, true, true);
        ex.addSuppressed(new IllegalArgumentException("Suppressed"));
        assertThat(ex.getSuppressed()).hasSize(1);
    }
}
