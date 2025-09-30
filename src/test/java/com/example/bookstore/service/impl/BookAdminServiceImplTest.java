package com.example.bookstore.service.impl;


import com.example.bookstore.domain.Author;
import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.Genre;
import com.example.bookstore.dto.BookRequest;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.exception.BookStoreException;
import com.example.bookstore.kafka.producer.BookEventPublisher;
import com.example.bookstore.service.AuthorService;
import com.example.bookstore.service.BookService;
import com.example.bookstore.service.GenreService;
import com.example.bookstore.service.filter.BookFilter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class BookAdminServiceImplTest {

    private BookService bookService;
    private AuthorService authorService;
    private GenreService genreService;
    private BookEventPublisher bookEventPublisher;
    private BookAdminServiceImpl service;
    private MeterRegistry meterRegistry;

    private Counter createCounter;
    private Counter updateCounter;
    private Counter deleteCounter;
    @BeforeEach
    void setup() {
        bookService = mock(BookService.class);
        authorService = mock(AuthorService.class);
        genreService = mock(GenreService.class);
        bookEventPublisher = mock(BookEventPublisher.class);
        meterRegistry = mock(MeterRegistry.class);
        createCounter = mock(Counter.class);
        updateCounter = mock(Counter.class);
        deleteCounter = mock(Counter.class);


    //    when(meterRegistry.counter("book.create.count")).thenReturn(createCounter);
      //  when(meterRegistry.counter("book.update.count")).thenReturn(updateCounter);

        service = new BookAdminServiceImpl(bookService, authorService, genreService, bookEventPublisher,meterRegistry);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "createCounter", createCounter);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "updateCounter", updateCounter);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "deleteCounter", deleteCounter);

    }
    private static java.util.Set<String> toStringTags(Iterable<Tag> tags) {
        java.util.Set<String> out = new java.util.HashSet<>();
        for (Tag t : tags) out.add(t.getKey() + "=" + t.getValue());
        return out;
    }

    @Test
    void shouldListBooksWithFilter() {
        BookFilter filter =
                new BookFilter("query", "title", "author", "genre", BigDecimal.TEN, BigDecimal.valueOf(100));
        Pageable pageable = PageRequest.of(0, 10);
        Book book = new Book();
        book.setId(1L);
        when(bookService.findAll(any(), any())).thenReturn(new PageImpl<>(List.of(book)));

        Page<BookResponse> result = service.list(filter, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldCreateBookSuccessfully() {
        BookRequest request = BookRequest.builder().authorId(1L).genreId(2L).title("New Book").build();
        Author author = new Author();
        author.setId(1L);
        author.setName("Geine");
        Genre genre = new Genre();
        genre.setId(2L);
        genre.setName("Philosophie");

        Book book = new Book();
        book.setId(10L);
        book.setTitle("New Book");
        book.setAuthor(author);
        book.setGenre(genre);
        when(authorService.findById(1L)).thenReturn(author);
        when(genreService.findById(2L)).thenReturn(genre);
        when(bookService.saveAndFlush(any())).thenReturn(book);

        BookResponse response = service.create(request);

        verify(createCounter, times(1)).increment();
        verifyNoMoreInteractions(createCounter);
        verifyNoInteractions(updateCounter);
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("New Book");
    }

    @Test
    void shouldUpdateBookSuccessfully() {
        BookRequest request = BookRequest.builder().authorId(1L).genreId(2L).title("Updated Book").build();
        Author author = new Author();
        Genre genre = new Genre();
        Book existing = new Book();
        existing.setId(5L);
        existing.setTitle("Old Title");

        when(authorService.findById(1L)).thenReturn(author);
        when(genreService.findById(2L)).thenReturn(genre);
        when(bookService.findById(5L)).thenReturn(existing);
        when(bookService.saveAndFlush(any())).thenReturn(existing);

        BookResponse response = service.update(5L, request);

        verify(updateCounter, times(1)).increment();
        verifyNoMoreInteractions(createCounter);
     //   verifyNoInteractions(updateCounter);
        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getTitle()).isEqualTo("Updated Book");
    }

    @Test
    void shouldGetBookById() {
        Book book = new Book();
        book.setId(7L);
        book.setTitle("Get Me");
        when(bookService.findById(7L)).thenReturn(book);

        BookResponse response = service.get(7L);

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getTitle()).isEqualTo("Get Me");
    }

    @Test
    void shouldDeleteBookSuccessfully() {
        Book book = new Book();
        book.setId(8L);
        when(bookService.findById(8L)).thenReturn(book);

        service.delete(8L);

        verify(deleteCounter, times(1)).increment();
        verifyNoMoreInteractions(createCounter);
        verifyNoMoreInteractions(updateCounter);

        verify(bookService).delete(book);
    }

    @Test
    void shouldPublishEventAfterCreate() {
        BookRequest request = BookRequest.builder().authorId(1L).genreId(2L).title("Kafka Book").build();
        Author author = new Author();
        author.setName("Geine");
        Genre genre = new Genre();
        genre.setName("Philosophie");
        Book book = new Book();
        book.setId(9L);
        book.setTitle("Kafka Book");
        book.setAuthor(author);
        book.setGenre(genre);
        when(authorService.findById(1L)).thenReturn(author);
        when(genreService.findById(2L)).thenReturn(genre);
        when(bookService.saveAndFlush(any())).thenReturn(book);
     //   org.springframework.test.util.ReflectionTestUtils.setField(service, "createCounter", createCounter);
     //   org.springframework.test.util.ReflectionTestUtils.setField(service, "updateCounter", updateCounter);
        service.create(request);
        verify(createCounter, times(1)).increment();
        verifyNoMoreInteractions(createCounter);
        verifyNoInteractions(updateCounter);
        verify(bookEventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    void shouldPublishEventAfterUpdate() {
        BookRequest request = BookRequest.builder().authorId(1L).genreId(2L).title("Kafka Update").build();
        Author author = new Author();
        Genre genre = new Genre();
        Book book = new Book();
        book.setId(10L);

        when(authorService.findById(1L)).thenReturn(author);
        when(genreService.findById(2L)).thenReturn(genre);
        when(bookService.findById(10L)).thenReturn(book);
        when(bookService.saveAndFlush(any())).thenReturn(book);

        service.update(10L, request);
        verify(updateCounter, times(1)).increment();
        verifyNoMoreInteractions(createCounter);
        verify(bookEventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    void shouldPublishEventAfterDelete() {
        Book book = new Book();
        book.setId(11L);
        when(bookService.findById(11L)).thenReturn(book);

        service.delete(11L);
        verify(deleteCounter, times(1)).increment();
        verifyNoMoreInteractions(createCounter);
        verifyNoMoreInteractions(updateCounter);
        verify(bookEventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    void shouldMapBookToResponseCorrectly() {
        Book book = new Book();
        book.setId(12L);
        book.setTitle("Mapped Book");
        when(bookService.findById(12L)).thenReturn(book);

        BookResponse response = service.get(12L);

        assertThat(response.getTitle()).isEqualTo("Mapped Book");
    }

    @Test
    void shouldThrowExceptionWhenAuthorNotFoundOnCreate() {
        BookRequest request = BookRequest.builder().authorId(99L).genreId(1L).build();
        when(authorService.findById(99L)).thenThrow(new BookStoreException("Author not found"));

        assertThrows(BookStoreException.class, () -> service.create(request));
    }

    @Test
    void shouldThrowExceptionWhenGenreNotFoundOnCreate() {
        BookRequest request = BookRequest.builder().authorId(1L).genreId(99L).build();
        when(authorService.findById(1L)).thenReturn(new Author());
        when(genreService.findById(99L)).thenThrow(new BookStoreException("Genre not found"));

        assertThrows(BookStoreException.class, () -> service.create(request));
    }

    @Test
    void shouldThrowExceptionOnSaveFailureDuringCreate() {
        BookRequest request = BookRequest.builder().authorId(1L).genreId(2L).title("Fail Book").build();
        when(authorService.findById(1L)).thenReturn(new Author());
        when(genreService.findById(2L)).thenReturn(new Genre());
        when(bookService.saveAndFlush(any())).thenThrow(new RuntimeException("DB error"));

        assertThrows(BookStoreException.class, () -> service.create(request));

        verifyNoMoreInteractions(createCounter);
        verifyNoMoreInteractions(updateCounter);
        verifyNoMoreInteractions(deleteCounter);
    }

    @Test
    void shouldThrowExceptionOnSaveFailureDuringUpdate() {
        BookRequest request = BookRequest.builder().authorId(1L).genreId(2L).title("Fail Update").build();
        when(authorService.findById(1L)).thenReturn(new Author());
        when(genreService.findById(2L)).thenReturn(new Genre());
        when(bookService.findById(1L)).thenReturn(new Book());
        when(bookService.saveAndFlush(any())).thenThrow(new RuntimeException("DB error"));

        assertThrows(BookStoreException.class, () -> service.update(1L, request));
        verifyNoMoreInteractions(createCounter);
        verifyNoMoreInteractions(updateCounter);
        verifyNoMoreInteractions(deleteCounter);
    }

    @Test
    void shouldThrowExceptionWhenBookNotFoundOnUpdate() {
        BookRequest request = BookRequest.builder().authorId(1L).genreId(2L).build();
        when(authorService.findById(1L)).thenReturn(new Author());
        when(genreService.findById(2L)).thenReturn(new Genre());
        when(bookService.findById(1L)).thenThrow(new BookStoreException("Book not found"));

        assertThrows(BookStoreException.class, () -> service.update(1L, request));
    }

    @Test
    void shouldThrowExceptionWhenBookNotFoundOnDelete() {
        when(bookService.findById(99L)).thenThrow(new BookStoreException("Book not found"));

        assertThrows(BookStoreException.class, () -> service.delete(99L));
    }

}