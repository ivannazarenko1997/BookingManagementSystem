package com.example.bookstore.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.bookstore.cache.BookCache;
import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.BookIndexProjection;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.search.model.BookDocument;
import com.example.bookstore.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

class BookServiceImplPositiveTest {

    private BookRepository bookRepository;
    private BookCache bookCache;
    private BookService service;

    @BeforeEach
    void setup() {
        bookRepository = mock(BookRepository.class);
        bookCache = mock(BookCache.class);
        service = new BookServiceImpl(bookRepository, bookCache);
    }

    @Test
    void shouldFindBookById() {
        Book book = new Book();
        book.setId(1L);
        when(bookRepository.findById(1L)).thenReturn(java.util.Optional.of(book));

        Book result = service.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }


    @Test
    void shouldSaveAndFlushBook() {
        Book book = new Book();
        book.setId(3L);
        when(bookRepository.saveAndFlush(book)).thenReturn(book);

        Book result = service.saveAndFlush(book);

        assertThat(result.getId()).isEqualTo(3L);
    }

    @Test
    void shouldDeleteBook() {
        Book book = new Book();
        book.setId(4L);

        service.delete(book);

        verify(bookRepository).delete(book);
    }

    @Test
    void shouldGetDocumentsByIdsFromCacheOnly() {
        BookDocument doc = new BookDocument();
        doc.setId(6L);
        when(bookCache.getAllByIds(List.of(6L))).thenReturn(List.of(doc));

        List<BookDocument> result = service.getDocumentsByIds(List.of(6L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(6L);
    }

    @Test
    void shouldGetDocumentsByIdsWithMissingBooks() {
        BookDocument cached = new BookDocument();
        cached.setId(7L);
        Book missingBook = new Book();
        missingBook.setId(8L);
        when(bookCache.getAllByIds(List.of(7L, 8L))).thenReturn(List.of(cached));
        when(bookRepository.findAllById(List.of(8L))).thenReturn(List.of(missingBook));

        List<BookDocument> result = service.getDocumentsByIds(List.of(7L, 8L));

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldPutMissingDocumentsIntoCache() {
        Book missingBook = new Book();
        missingBook.setId(9L);
        when(bookCache.getAllByIds(List.of(9L))).thenReturn(List.of());
        when(bookRepository.findAllById(List.of(9L))).thenReturn(List.of(missingBook));

        service.getDocumentsByIds(List.of(9L));

        verify(bookCache).putAll(any());
    }

    @Test
    void shouldFindBooksForIndexing() {
        BookIndexProjection projection = mock(BookIndexProjection.class);
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findAllForIndexing(pageable)).thenReturn(new PageImpl<>(List.of(projection)));

        Page<BookIndexProjection> result = service.findBooksForIndexing(pageable);

        assertThat(result.getContent()).hasSize(1);
    }
}
