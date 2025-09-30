package com.example.bookstore.service.impl;


import com.example.bookstore.domain.Author;
import com.example.bookstore.exception.BookStoreException;
import com.example.bookstore.repository.AuthorRepository;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.GenreRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(AuthorServiceImpl.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class AuthorServiceImplIntegrationIT {

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

    @Autowired private AuthorServiceImpl authorService;
    @Autowired private AuthorRepository authorRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private GenreRepository genreRepository;
    @BeforeEach
    void cleanDb() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();
    }

    @Test
    void findById_returnsAuthor_whenExists() {

        Author author = new Author();
        author.setName("George Orwell 6");
        author = authorRepository.saveAndFlush(author);

        Author foundAuthor = authorService.findById(author.getId());

        assertThat(foundAuthor).isNotNull();
        assertThat(foundAuthor.getId()).isEqualTo(author.getId());
        assertThat(foundAuthor.getName()).isEqualTo("George Orwell 6");
    }

    @Test
    void findById_throwsException_whenNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> authorService.findById(nonExistentId))
                .isInstanceOf(BookStoreException.class)
                .hasMessageContaining("Author not found: " + nonExistentId);
    }
}
