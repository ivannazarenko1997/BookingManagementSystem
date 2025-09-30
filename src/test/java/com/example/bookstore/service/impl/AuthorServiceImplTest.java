package com.example.bookstore.service.impl;

import com.example.bookstore.domain.Author;
import com.example.bookstore.exception.BookStoreException;
import com.example.bookstore.repository.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AuthorServiceImplTest {

    private AuthorRepository authorRepository;
    private AuthorServiceImpl authorService;

    @BeforeEach
    void setUp() {
        authorRepository = mock(AuthorRepository.class);
        authorService = new AuthorServiceImpl(authorRepository);
    }

    @Test
    @DisplayName("Should return author when found by ID")
    void findById_validId_returnsAuthor() {
        Author author = new Author();
        author.setId(1L);
        author.setName("Jane Austen");

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        Author result = authorService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Jane Austen");
    }

    @Test
    @DisplayName("Should return correct author for different ID")
    void findById_differentId_returnsCorrectAuthor() {
        Author author = new Author();
        author.setId(2L);
        author.setName("Mark Twain");

        when(authorRepository.findById(2L)).thenReturn(Optional.of(author));

        Author result = authorService.findById(2L);

        assertThat(result.getName()).isEqualTo("Mark Twain");
    }

    @Test
    @DisplayName("Should call repository exactly once")
    void findById_invokesRepositoryOnce() {
        Author author = new Author();
        author.setId(3L);
        author.setName("Leo Tolstoy");

        when(authorRepository.findById(3L)).thenReturn(Optional.of(author));

        authorService.findById(3L);

        verify(authorRepository, times(1)).findById(3L);
    }

    @Test
    @DisplayName("Should preserve author object integrity")
    void findById_preservesAuthorFields() {
        Author author = new Author();
        author.setId(4L);
        author.setName("George Orwell");

        when(authorRepository.findById(4L)).thenReturn(Optional.of(author));

        Author result = authorService.findById(4L);

        assertThat(result.getId()).isEqualTo(4L);
        assertThat(result.getName()).isEqualTo("George Orwell");
    }

    @Test
    @DisplayName("Should throw exception when author not found")
    void findById_authorNotFound_throwsException() {
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        BookStoreException ex = assertThrows(BookStoreException.class, () -> authorService.findById(99L));
        assertThat(ex.getMessage()).isEqualTo("Author not found: 99");
    }

    @Test
    @DisplayName("Should throw exception for null ID")
    void findById_nullId_throwsIllegalArgumentException() {
        when(authorRepository.findById(null)).thenThrow(new IllegalArgumentException("ID cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> authorService.findById(null));
    }

    @Test
    @DisplayName("Should throw runtime exception if repository fails")
    void findById_repositoryThrows_throwsRuntimeException() {
        when(authorRepository.findById(5L)).thenThrow(new RuntimeException("Database error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authorService.findById(5L));
        assertThat(ex.getMessage()).isEqualTo("Database error");
    }

    @Test
    @DisplayName("Should not return author when repository returns empty optional")
    void findById_emptyOptional_throwsBookStoreException() {
        when(authorRepository.findById(6L)).thenReturn(Optional.empty());

        assertThrows(BookStoreException.class, () -> authorService.findById(6L));
    }
}
