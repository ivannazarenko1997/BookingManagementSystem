package com.example.bookstore.service.impl;


import com.example.bookstore.cache.BookCache;
import com.example.bookstore.domain.Author;
import com.example.bookstore.domain.Genre;
import com.example.bookstore.dto.BookRequest;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.kafka.producer.BookEventPublisher;
import com.example.bookstore.repository.AuthorRepository;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.GenreRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;

@DataJpaTest
@Import({
        BookAdminServiceImpl.class,
        BookServiceImpl.class,
        AuthorServiceImpl.class,
        GenreServiceImpl.class,
        BookAdminServiceImplIntegrationIT.TestMetricsConfig.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers

class BookAdminServiceImplIntegrationIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        r.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired private BookAdminServiceImpl bookAdminService;
    @Autowired private AuthorRepository authorRepository;
    @Autowired private GenreRepository genreRepository;
    @Autowired private BookRepository bookRepository;

    @MockBean private BookEventPublisher bookEventPublisher;
    @MockBean private BookCache bookCache;

    @BeforeEach
    void setup() {
        // BookCache is used by services; make it safe no-op
        doNothing().when(bookCache).putAll(anyCollection());
        doNothing().when(bookCache).evict(anyLong());
        doNothing().when(bookCache).clear();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();
    }

    @Test
    void shouldCreateBookSuccessfully() {
        Author author = persistAuthor("J.K. Rowling 0");
        Genre genre = persistGenre("Fantasy 0");

        BookRequest request = BookRequest.builder()
                .title("Harry Potter")
                .authorId(author.getId())
                .genreId(genre.getId())
                .description("Magic tale")
                .price(BigDecimal.valueOf(29.99))
                .build();

        BookResponse response = bookAdminService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Harry Potter");
        assertThat(bookRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldUpdateBookSuccessfully() {
        Author author = persistAuthor("J.K. Rowling 1");
        Genre genre = persistGenre("Fantasy 1");

        BookResponse created = bookAdminService.create(
                BookRequest.builder()
                        .title("Harry Potter")
                        .authorId(author.getId())
                        .genreId(genre.getId())
                        .description("Magic tale")
                        .price(BigDecimal.valueOf(29.99))
                        .build()
        );

        BookRequest updateRequest = BookRequest.builder()
                .title("Harry Potter")
                .authorId(author.getId())
                .genreId(genre.getId())
                .description("Magic tale 2")
                .price(BigDecimal.valueOf(19.99))
                .build();

        BookResponse updated = bookAdminService.update(created.getId(), updateRequest);

        assertThat(updated.getTitle()).isEqualTo("Harry Potter");
        assertThat(updated.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(19.99));
    }

    @Test
    void shouldGetBookSuccessfully() {
        Author author = persistAuthor("J.K. Rowling 2");
        Genre genre = persistGenre("Fantasy 2");

        BookResponse created = bookAdminService.create(
                BookRequest.builder()
                        .title("Harry Potter")
                        .authorId(author.getId())
                        .genreId(genre.getId())
                        .description("Magic tale")
                        .price(BigDecimal.valueOf(29.99))
                        .build()
        );

        BookResponse fetched = bookAdminService.get(created.getId());
        assertThat(fetched.getTitle()).isEqualTo("Harry Potter");
    }

    @Test
    void shouldDeleteBookSuccessfully() {
        Author author = persistAuthor("J.K. Rowling 3");
        Genre genre = persistGenre("Fantasy 3");

        BookResponse created = bookAdminService.create(
                BookRequest.builder()
                        .title("Harry Potter")
                        .authorId(author.getId())
                        .genreId(genre.getId())
                        .description("Magic tale")
                        .price(BigDecimal.valueOf(29.99))
                        .build()
        );

        bookAdminService.delete(created.getId());

        // assert using existsById; avoids needing flush in same tx
        assertThat(bookRepository.existsById(created.getId())).isFalse();
    }

    private Author persistAuthor(String name) {
        Author a = new Author();
        a.setName(name);
        return authorRepository.save(a);
    }

    private Genre persistGenre(String name) {
        Genre g = new Genre();
        g.setName(name);
        return genreRepository.save(g);
    }

    @TestConfiguration
    static class TestMetricsConfig {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }
}
