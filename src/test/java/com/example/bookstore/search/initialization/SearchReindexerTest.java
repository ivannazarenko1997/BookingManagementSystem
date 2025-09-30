package com.example.bookstore.search.initialization;


import com.example.bookstore.domain.BookIndexProjection;
import com.example.bookstore.search.model.BookDocument;
import com.example.bookstore.search.repository.BookSearchRepository;
import com.example.bookstore.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

class SearchReindexerTest {

    private BookService bookService;
    private BookSearchRepository searchRepository;
    private SearchReindexer reindexer;

    @BeforeEach
    void setup() {
        bookService = mock(BookService.class);
        searchRepository = mock(BookSearchRepository.class);
        reindexer = new SearchReindexer(bookService, searchRepository);

        setField("reindexOnStart", true);
        setField("batchSize", 2);
        setField("failOnError", false);
    }

    private void setField(String name, Object value) {
        try {
            var field = SearchReindexer.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(reindexer, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldSkipReindexIfDisabled() throws Exception {
        setField("reindexOnStart", false);

        ApplicationArguments args = mock(ApplicationArguments.class);
        reindexer.run(args);

        verifyNoInteractions(bookService);
        verifyNoInteractions(searchRepository);
    }

    @Test
    void shouldReindexBooksWhenEnabled() throws Exception {


        BookIndexProjection projection1 = mock(BookIndexProjection.class);
        when(projection1.getId()).thenReturn(1L);
        when(projection1.getTitle()).thenReturn("Test Book");
        when(projection1.getAuthorName()).thenReturn("Jane Doe");
        when(projection1.getGenreName()).thenReturn("Fiction");
        when(projection1.getPrice()).thenReturn(BigDecimal.valueOf(19.99));


        BookIndexProjection projection2 = mock(BookIndexProjection.class);
        when(projection2.getId()).thenReturn(2L);
        when(projection2.getTitle()).thenReturn("Test Book Two");
        when(projection2.getAuthorName()).thenReturn("Jane Doe Two");
        when(projection2.getGenreName()).thenReturn("Fiction Two");
        when(projection2.getPrice()).thenReturn(BigDecimal.valueOf(19.99));

        Page<BookIndexProjection> page = new PageImpl<>(List.of(projection1, projection2), PageRequest.of(0, 2), 2);
        when(bookService.findBooksForIndexing(PageRequest.of(0, 2))).thenReturn(page);
        when(bookService.findBooksForIndexing(PageRequest.of(1, 2))).thenReturn(Page.empty());

        ApplicationArguments args = mock(ApplicationArguments.class);
        reindexer.run(args);

        ArgumentCaptor<List<BookDocument>> captor = ArgumentCaptor.forClass(List.class);
        verify(searchRepository).saveAll(captor.capture());

        List<BookDocument> savedDocs = captor.getValue();
        assertThat(savedDocs).hasSize(2);
        assertThat(savedDocs.get(0).getTitle()).isEqualTo("Test Book");
        assertThat(savedDocs.get(1).getTitle()).isEqualTo("Test Book Two");
    }

    @Test
    void shouldHandleExceptionGracefullyWhenFailOnErrorFalse() throws Exception {
        when(bookService.findBooksForIndexing(any())).thenThrow(new RuntimeException("Boom"));

        ApplicationArguments args = mock(ApplicationArguments.class);
        reindexer.run(args);

        verify(searchRepository, never()).saveAll(any());
    }

    @Test
    void shouldThrowExceptionWhenFailOnErrorTrue() {
        setField("failOnError", true);
        when(bookService.findBooksForIndexing(any())).thenThrow(new RuntimeException("Boom"));

        ApplicationArguments args = mock(ApplicationArguments.class);
        assertThrows(RuntimeException.class, () -> reindexer.run(args));
    }
}
