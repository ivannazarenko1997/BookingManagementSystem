package com.example.bookstore.service.impl;

import com.example.bookstore.cache.BookCache;
import com.example.bookstore.domain.Author;
import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.BookIndexProjection;
import com.example.bookstore.domain.Genre;
import com.example.bookstore.exception.BookStoreException;
import com.example.bookstore.repository.AuthorRepository;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.GenreRepository;
import com.example.bookstore.search.model.BookDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@DataJpaTest
@Import(BookServiceImpl.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class BookServiceImplIntegrationIT {

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

    @Autowired private BookServiceImpl bookService;

    @Autowired private BookRepository bookRepository;
    @Autowired private AuthorRepository authorRepository;
    @Autowired private GenreRepository genreRepository;

    @MockBean private BookCache bookCache;

    @BeforeEach
    void init() {
        bookRepository.deleteAll();
        genreRepository.deleteAll();
        authorRepository.deleteAll();
    }

    @Test
    void getDocumentsByIds_allFromCache_returnsCacheOnly() {
        Author author = persistAuthor("J.K. Rowling 1");
        Genre genre = persistGenre("Fantasy 1");
        Book book1 = persistBook("HP1", author, genre, BigDecimal.TEN);
        Book book2 = persistBook("HP2", author, genre, BigDecimal.valueOf(12));

        BookDocument doc1 = new BookDocument(book1.getId(), book1.getTitle(), author.getName(), genre.getName(), book1.getPrice());
        BookDocument doc2 = new BookDocument(book2.getId(), book2.getTitle(), author.getName(), genre.getName(), book2.getPrice());

        when(bookCache.getAllByIds(List.of(book1.getId(), book2.getId()))).thenReturn(List.of(doc1, doc2));

        var result = bookService.getDocumentsByIds(List.of(book1.getId(), book2.getId()));

        assertThat(result).containsExactlyInAnyOrder(doc1, doc2);
        verify(bookCache).getAllByIds(List.of(book1.getId(), book2.getId()));
        verify(bookCache, never()).putAll(anyCollection());
    }

    @Test
    void getDocumentsByIds_partialCache_fetchesMissing() {
        Author author = persistAuthor("J.K. Rowling 2");
        Genre genre = persistGenre("Fantasy 2");
        Book book1 = persistBook("HP1", author, genre, BigDecimal.TEN);
        Book book2 = persistBook("HP2", author, genre, BigDecimal.valueOf(12));
        Book book3 = persistBook("HP3", author, genre, BigDecimal.valueOf(14));

        BookDocument cachedDoc = new BookDocument(book1.getId(), book1.getTitle(), author.getName(), genre.getName(), book1.getPrice());
        when(bookCache.getAllByIds(List.of(book1.getId(), book2.getId(), book3.getId())))
                .thenReturn(List.of(cachedDoc));

        var result = bookService.getDocumentsByIds(List.of(book1.getId(), book2.getId(), book3.getId()));

        ArgumentCaptor<List<BookDocument>> captor = ArgumentCaptor.forClass(List.class);
        verify(bookCache).putAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
        assertThat(captor.getValue()).extracting(BookDocument::getId)
                .containsExactlyInAnyOrder(book2.getId(), book3.getId());

        assertThat(result).extracting(BookDocument::getId)
                .containsExactlyInAnyOrder(book1.getId(), book2.getId(), book3.getId());
    }

    @Test
    void findById_returnsBook() {
        Author author = persistAuthor("J.K. Rowling 3");
        Genre genre = persistGenre("Fantasy 3");
        Book book1 = persistBook("HPX", author, genre, BigDecimal.valueOf(17.5));
        Book found = bookService.findById(book1.getId());
        assertThat(found.getTitle()).isEqualTo("HPX");
    }

    @Test
    void findById_throwsWhenNotFound() {
        assertThatThrownBy(() -> bookService.findById(999L))
                .isInstanceOf(BookStoreException.class)
                .hasMessageContaining("Book not found");
    }

    @Test
    void findBooksForIndexing_returnsPage() {
        Author author = persistAuthor("J.K. Rowling 4");
        Genre genre = persistGenre("Fantasy 4");
        persistBook("HP4", author, genre, BigDecimal.valueOf(20));
        persistBook("HP5", author, genre, BigDecimal.valueOf(21));
        persistBook("HP6", author, genre, BigDecimal.valueOf(22));

        Page<BookIndexProjection> page = bookService.findBooksForIndexing(PageRequest.of(0, 2));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(3);
    }

    // helpers
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

    private Book persistBook(String title, Author a, Genre g, BigDecimal price) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(a);
        book.setGenre(g);
        book.setPrice(price);
        return bookRepository.saveAndFlush(book);
    }
}
