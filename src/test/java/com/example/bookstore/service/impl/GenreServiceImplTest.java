package com.example.bookstore.service.impl;

import com.example.bookstore.domain.Genre;
import com.example.bookstore.exception.BookStoreException;
import com.example.bookstore.repository.GenreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GenreServiceImplTest {

    private GenreRepository genreRepository;
    private GenreServiceImpl genreService;

    @BeforeEach
    void setUp() {
        genreRepository = mock(GenreRepository.class);
        genreService = new GenreServiceImpl(genreRepository);
    }

    // ✅ Positive cases

    @Test
    void shouldReturnGenreWhenFound() {
        Genre genre = new Genre();
        genre.setId(1L);
        genre.setName("Fantasy");

        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));

        Genre result = genreService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Fantasy");
    }

    @Test
    void shouldCallRepositoryOnce() {
        Genre genre = new Genre();
        genre.setId(2L);

        when(genreRepository.findById(2L)).thenReturn(Optional.of(genre));

        genreService.findById(2L);

        verify(genreRepository, times(1)).findById(2L);
    }

    @Test
    void shouldPreserveGenreFields() {
        Genre genre = new Genre();
        genre.setId(3L);
        genre.setName("Science Fiction");

        when(genreRepository.findById(3L)).thenReturn(Optional.of(genre));

        Genre result = genreService.findById(3L);

        assertThat(result.getName()).isEqualTo("Science Fiction");
    }

    @Test
    void shouldReturnDifferentGenreById() {
        Genre genre = new Genre();
        genre.setId(4L);
        genre.setName("Mystery");

        when(genreRepository.findById(4L)).thenReturn(Optional.of(genre));

        Genre result = genreService.findById(4L);

        assertThat(result.getName()).isEqualTo("Mystery");
    }

    // ❌ Negative cases

    @Test
    void shouldThrowExceptionWhenGenreNotFound() {
        when(genreRepository.findById(99L)).thenReturn(Optional.empty());

        BookStoreException ex = assertThrows(BookStoreException.class, () -> genreService.findById(99L));
        assertThat(ex.getMessage()).isEqualTo("Genre not found: 99");
    }

    @Test
    void shouldThrowExceptionForNullId() {
        when(genreRepository.findById(null)).thenThrow(new IllegalArgumentException("ID cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> genreService.findById(null));
    }

    @Test
    void shouldThrowExceptionWhenRepositoryFails() {
        when(genreRepository.findById(5L)).thenThrow(new RuntimeException("Database error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> genreService.findById(5L));
        assertThat(ex.getMessage()).isEqualTo("Database error");
    }

    @Test
    void shouldNotReturnGenreWhenOptionalIsEmpty() {
        when(genreRepository.findById(6L)).thenReturn(Optional.empty());

        assertThrows(BookStoreException.class, () -> genreService.findById(6L));
    }
}
