package com.example.bookstore.search.service;



import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import com.example.bookstore.search.dto.BookSearchItem;
import com.example.bookstore.search.model.BookDocument;
import com.example.bookstore.search.service.impl.BookSearchCustomServiceImpl;
import com.example.bookstore.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

class BookSearchCustomServiceImplTest {

    private ElasticsearchClient elasticsearchClient;
    private BookService bookService;
    private BookSearchCustomServiceImpl service;
    @BeforeEach
    void setup() {
        elasticsearchClient = mock(ElasticsearchClient.class);
        bookService = mock(BookService.class);
        service = new BookSearchCustomServiceImpl(elasticsearchClient, bookService);
    }

    @Test
    void shouldReturnResultsForQueryText() throws Exception {
        BookDocument doc = new BookDocument();
        doc.setId(1L);
        doc.setTitle("Spring Boot");

        mockSearchResponse(doc);
        when(bookService.getDocumentsByIds(List.of(1L))).thenReturn(List.of(doc));

        Page<BookSearchItem> result = service.searchBooks("Spring", null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Spring Boot");
    }

    @Test
    void shouldReturnEmptyPageWhenNoHits() throws Exception {
        mockSearchResponse();
        Page<BookSearchItem> result = service.searchBooks("NoMatch", null, null, null, null, null, PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void shouldHandleSearchExceptionGracefully() throws Exception {
         when(elasticsearchClient.search(any(Function.class), eq(BookDocument.class)))
                .thenThrow(new RuntimeException("Search failed"));
        Page<BookSearchItem> result = service.searchBooks("error", null, null, null, null, null, PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void shouldSortByTitleAscending() throws Exception {
        BookDocument doc1 = new BookDocument();
        doc1.setId(1L);
        doc1.setTitle("A");

        BookDocument doc2 = new BookDocument();
        doc2.setId(2L);
        doc2.setTitle("Z");

        mockSearchResponse(doc1, doc2);
        when(bookService.getDocumentsByIds(List.of(1L, 2L))).thenReturn(List.of(doc2, doc1));

        Sort sort = Sort.by(Sort.Order.asc("title"));
        Page<BookSearchItem> result = service.searchBooks(null, null, null, null, null, null, PageRequest.of(0, 10, sort));

        assertThat(result.getContent().get(0).getTitle()).isEqualTo("A");
    }

    @Test
    void shouldIgnoreUnknownSortProperty() throws Exception {
        BookDocument doc = new BookDocument();
        doc.setId(3L);
        doc.setTitle("Unknown");

        mockSearchResponse(doc);
        when(bookService.getDocumentsByIds(List.of(3L))).thenReturn(List.of(doc));

        Sort sort = Sort.by(Sort.Order.asc("unknown"));
        Page<BookSearchItem> result = service.searchBooks(null, null, null, null, null, null, PageRequest.of(0, 10, sort));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldHandleNullSearchResponse() throws Exception {

        when(elasticsearchClient.search(any(Function.class), eq(BookDocument.class)))
                .thenReturn(null);
        Page<BookSearchItem> result = service.searchBooks("nullResponse", null, null, null, null, null, PageRequest.of(0, 10));
        assertThat(result.getContent()).isEmpty();
    }

    private void mockSearchResponse(BookDocument... docs) throws Exception {
        List<Hit<BookDocument>> hitList = new ArrayList<>();
        for (BookDocument doc : docs) {
            Hit<BookDocument> hit = mock(Hit.class);
            when(hit.source()).thenReturn(doc);
            hitList.add(hit);
        }

        TotalHits totalHits = new TotalHits.Builder()
                .value((long) hitList.size())
                .relation(TotalHitsRelation.Eq)
                .build();

        HitsMetadata<BookDocument> hitsMetadata = mock(HitsMetadata.class);
        when(hitsMetadata.hits()).thenReturn(hitList);
        when(hitsMetadata.total()).thenReturn(totalHits);

        SearchResponse<BookDocument> response = mock(SearchResponse.class);
        when(response.hits()).thenReturn(hitsMetadata);

        when(elasticsearchClient.search(any(Function.class), eq(BookDocument.class))).thenReturn(response);
    }
}
