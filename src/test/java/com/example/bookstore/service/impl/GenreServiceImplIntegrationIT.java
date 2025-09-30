package com.example.bookstore.service.impl;

import com.example.bookstore.domain.Genre;
import com.example.bookstore.exception.BookStoreException;
import com.example.bookstore.repository.AuthorRepository;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.GenreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(GenreServiceImpl.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class GenreServiceImplIntegrationIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired private GenreServiceImpl genreService;
    @Autowired private GenreRepository genreRepository;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private BookRepository bookRepository;
    @Autowired private AuthorRepository authorRepository;
    @BeforeEach
    void init() {
        bookRepository.deleteAll();
        genreRepository.deleteAll();
        authorRepository.deleteAll();
    }

    @Test
    void findById_returnsGenre_whenExists() {
        Genre genre = new Genre();
        genre.setName("Fantasy 9");
        genre = genreRepository.saveAndFlush(genre);

        Genre foundGenre = genreService.findById(genre.getId());

        assertThat(foundGenre).isNotNull();
        assertThat(foundGenre.getId()).isEqualTo(genre.getId());
        assertThat(foundGenre.getName()).isEqualTo("Fantasy 9");
    }

    @Test
    void findById_throwsException_whenNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> genreService.findById(nonExistentId))
                .isInstanceOf(BookStoreException.class)
                .hasMessageContaining("Genre not found: " + nonExistentId);
    }
}
