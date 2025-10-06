package com.example.bookstore.kafka.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.example.bookstore.dto.BookSearchDto;
import com.example.bookstore.search.dto.BookSearchItem;
import com.example.bookstore.search.mapper.BookDocumentMapper;
import com.example.bookstore.search.model.BookDocument;
import com.example.bookstore.search.service.impl.BookSearchCustomServiceImpl;
import com.example.bookstore.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
class BookSearchCustomServiceImplTest {

    @org.mockito.Mock
    private ElasticsearchClient esClient;

    @org.mockito.Mock
    private BookService bookService;

    @Test
    @DisplayName("POS: sort by price ASC with nulls last and ID tiebreak")
    void pos_sortsByPriceAscNullsLastWithIdTiebreak() throws Exception {
        BookSearchCustomServiceImpl service = new BookSearchCustomServiceImpl(esClient, bookService);

        SearchResponse<BookDocument> esResponse = mockSearchResponseWithTotal(List.of(3L, 1L, 2L), 3L);
        when(esClient.search(any(Function.class), eq(BookDocument.class))).thenReturn(esResponse);

        BookDocument d1 = doc(1L, "A", "Au", "G", new BigDecimal("10.00"));
        BookDocument d2 = doc(2L, "B", "Au", "G", null);
        BookDocument d3 = doc(3L, "C", "Au", "G", new BigDecimal("10.00"));
        when(bookService.getDocumentsByIds(List.of(3L, 1L, 2L))).thenReturn(List.of(d1, d2, d3));

        try (MockedStatic<BookDocumentMapper> mocked = mockStatic(BookDocumentMapper.class)) {
            mocked.when(() -> BookDocumentMapper.toSearchItem(any(BookDocument.class)))
                    .thenAnswer(inv -> toItem((BookDocument) inv.getArgument(0)));
            BookSearchDto filters = BookSearchDto.builder() .build();
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("price")));
            Page<BookSearchItem> page = service.searchBooks(filters, pageable);

            assertThat(page.getTotalElements()).isEqualTo(3);
            assertThat(page.getContent()).extracting("id").containsExactly(1L, 3L, 2L);
        }
    }

    @Test
    @DisplayName("POS: sort by title ASC case-insensitive")
    void pos_sortsByTitleCaseInsensitive() throws Exception {
        BookSearchCustomServiceImpl service = new BookSearchCustomServiceImpl(esClient, bookService);

        SearchResponse<BookDocument> esResponse = mockSearchResponseWithTotal(List.of(1L, 2L, 3L), 3L);
        when(esClient.search(any(Function.class), eq(BookDocument.class))).thenReturn(esResponse);

        BookDocument d1 = doc(1L, "bETA", "Au", "G", new BigDecimal("5"));
        BookDocument d2 = doc(2L, "Alpha", "Au", "G", new BigDecimal("5"));
        BookDocument d3 = doc(3L, "gamma", "Au", "G", new BigDecimal("5"));
        when(bookService.getDocumentsByIds(List.of(1L, 2L, 3L))).thenReturn(List.of(d1, d2, d3));

        try (MockedStatic<BookDocumentMapper> mocked = mockStatic(BookDocumentMapper.class)) {
            mocked.when(() -> BookDocumentMapper.toSearchItem(any(BookDocument.class)))
                    .thenAnswer(inv -> toItem((BookDocument) inv.getArgument(0)));
            BookSearchDto filters = BookSearchDto.builder() .build();
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("title")));
            Page<BookSearchItem> page = service.searchBooks(filters, pageable);

            assertThat(page.getContent()).extracting("id").containsExactly(2L, 1L, 3L);
        }
    }

    @Test
    @DisplayName("POS: unsorted preserves original (hydrated) order")
    void pos_unsortedPreservesOriginalOrder() throws Exception {
        BookSearchCustomServiceImpl service = new BookSearchCustomServiceImpl(esClient, bookService);

        SearchResponse<BookDocument> esResponse = mockSearchResponseWithTotal(List.of(5L, 2L, 9L), 3L);
        when(esClient.search(any(Function.class), eq(BookDocument.class))).thenReturn(esResponse);

        BookDocument d5 = doc(5L, "T5", "A", "G", new BigDecimal("2"));
        BookDocument d2 = doc(2L, "T2", "A", "G", new BigDecimal("3"));
        BookDocument d9 = doc(9L, "T9", "A", "G", new BigDecimal("1"));
        when(bookService.getDocumentsByIds(List.of(5L, 2L, 9L))).thenReturn(List.of(d5, d2, d9));

        try (MockedStatic<BookDocumentMapper> mocked = mockStatic(BookDocumentMapper.class)) {
            mocked.when(() -> BookDocumentMapper.toSearchItem(any(BookDocument.class)))
                    .thenAnswer(inv -> toItem((BookDocument) inv.getArgument(0)));
            BookSearchDto filters = BookSearchDto.builder().build();
            Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());
            Page<BookSearchItem> page = service.searchBooks(filters, pageable);

            assertThat(page.getContent()).extracting("id").containsExactly(5L, 2L, 9L);
        }
    }

    @Test
    @DisplayName("POS: uses totalHits from Elasticsearch when present")
    void pos_respectsTotalFromElasticsearch() throws Exception {
        BookSearchCustomServiceImpl service = new BookSearchCustomServiceImpl(esClient, bookService);

        SearchResponse<BookDocument> esResponse = mockSearchResponseWithTotal(List.of(1L, 2L), 42L);
        when(esClient.search(any(Function.class), eq(BookDocument.class))).thenReturn(esResponse);

        BookDocument d1 = doc(1L, "A", "Au", "G", new BigDecimal("1"));
        BookDocument d2 = doc(2L, "B", "Au", "G", new BigDecimal("2"));
        when(bookService.getDocumentsByIds(List.of(1L, 2L))).thenReturn(List.of(d1, d2));

        try (MockedStatic<BookDocumentMapper> mocked = mockStatic(BookDocumentMapper.class)) {
            mocked.when(() -> BookDocumentMapper.toSearchItem(any(BookDocument.class)))
                    .thenAnswer(inv -> toItem((BookDocument) inv.getArgument(0)));
            BookSearchDto filters = BookSearchDto.builder().build();
            Pageable pageable = PageRequest.of(0, 2, Sort.unsorted());
            Page<BookSearchItem> page = service.searchBooks(filters, pageable);

            assertThat(page.getTotalElements()).isEqualTo(42);
            assertThat(page.getContent()).hasSize(2);
        }
    }

    @Test
    @DisplayName("NEG: returns empty page when Elasticsearch client throws")
    void neg_returnsEmptyOnEsException() throws Exception {
        BookSearchCustomServiceImpl service = new BookSearchCustomServiceImpl(esClient, bookService);
        when(esClient.search(any(Function.class), eq(BookDocument.class)))
                .thenThrow(new RuntimeException("es down"));
        BookSearchDto filters = BookSearchDto.builder().build();
        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());
        Page<BookSearchItem> page = service.searchBooks(filters, pageable);

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("NEG: returns empty page when BookService throws while hydrating")
    void neg_returnsEmptyOnBookServiceException() throws Exception {
        BookSearchCustomServiceImpl service = new BookSearchCustomServiceImpl(esClient, bookService);

        // Minimal ES response: only stubs hits().hits(); no total() to avoid unnecessary stubbing
        SearchResponse<BookDocument> esResponse = mockSearchResponseMinimal(List.of(10L, 20L));
        when(esClient.search(any(Function.class), eq(BookDocument.class))).thenReturn(esResponse);

        when(bookService.getDocumentsByIds(List.of(10L, 20L)))
                .thenThrow(new RuntimeException("db down"));
        BookSearchDto filters = BookSearchDto.builder().build();
        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());
        Page<BookSearchItem> page = service.searchBooks(filters, pageable);

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("NEG: returns empty page when mapping toSearchItem throws")
    void neg_returnsEmptyWhenMapperThrows() throws Exception {
        BookSearchCustomServiceImpl service = new BookSearchCustomServiceImpl(esClient, bookService);

        SearchResponse<BookDocument> esResponse = mockSearchResponseMinimal(List.of(1L));
        when(esClient.search(any(Function.class), eq(BookDocument.class))).thenReturn(esResponse);

        BookDocument d1 = doc(1L, "X", "Au", "G", new BigDecimal("5"));
        when(bookService.getDocumentsByIds(List.of(1L))).thenReturn(List.of(d1));

        try (MockedStatic<BookDocumentMapper> mocked = mockStatic(BookDocumentMapper.class)) {
            mocked.when(() -> BookDocumentMapper.toSearchItem(any(BookDocument.class)))
                    .thenThrow(new RuntimeException("mapper fail"));
            BookSearchDto filters = BookSearchDto.builder().build();
            Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());
            Page<BookSearchItem> page = service.searchBooks(filters, pageable);

            assertThat(page.getTotalElements()).isZero();
            assertThat(page.getContent()).isEmpty();
        }
    }

    @Test
    @DisplayName("NEG: unknown sort property is ignored (no crash, returns items)")
    void neg_unknownSortPropertyIgnored() throws Exception {
        BookSearchCustomServiceImpl service = new BookSearchCustomServiceImpl(esClient, bookService);

        SearchResponse<BookDocument> esResponse = mockSearchResponseWithTotal(List.of(4L, 1L, 3L), 3L);
        when(esClient.search(any(Function.class), eq(BookDocument.class))).thenReturn(esResponse);

        BookDocument d4 = doc(4L, "Z", "A", "G", new BigDecimal("3"));
        BookDocument d1 = doc(1L, "Y", "A", "G", new BigDecimal("2"));
        BookDocument d3 = doc(3L, "X", "A", "G", new BigDecimal("1"));
        when(bookService.getDocumentsByIds(List.of(4L, 1L, 3L))).thenReturn(List.of(d4, d1, d3));

        try (MockedStatic<BookDocumentMapper> mocked = mockStatic(BookDocumentMapper.class)) {
            mocked.when(() -> BookDocumentMapper.toSearchItem(any(BookDocument.class)))
                    .thenAnswer(inv -> toItem((BookDocument) inv.getArgument(0)));
            BookSearchDto filters = BookSearchDto.builder().build();
            Pageable pageable = PageRequest.of(0, 10, Sort.by("unknownProp"));
            Page<BookSearchItem> page = service.searchBooks(filters, pageable);

            assertThat(page.getContent()).extracting("id").containsExactly(4L, 1L, 3L);
        }
    }

    private static BookDocument doc(Long id, String title, String author, String genre, BigDecimal price) {
        BookDocument d = new BookDocument();
        d.setId(id);
        d.setTitle(title);
        d.setAuthorName(author);
        d.setGenreName(genre);
        d.setPrice(price);
        return d;
    }

    private static BookSearchItem toItem(BookDocument d) {
        return BookSearchItem.builder()
                .id(d.getId())
                .title(d.getTitle())
                .authorName(d.getAuthorName())
                .genreName(d.getGenreName())
                .price(d.getPrice())
                .build();
    }

    @SuppressWarnings("unchecked")
    private static SearchResponse<BookDocument> mockSearchResponseWithTotal(List<Long> ids, Long totalValue) {
        SearchResponse<BookDocument> resp = mock(SearchResponse.class);
        HitsMetadata<BookDocument> hits = mock(HitsMetadata.class);

        if (totalValue != null) {
            TotalHits total = mock(TotalHits.class);
            when(total.value()).thenReturn(totalValue);
            when(hits.total()).thenReturn(total);
        }

        List<Hit<BookDocument>> hitList = new ArrayList<>();
        for (Long id : ids) {
            Hit<BookDocument> h = mock(Hit.class);
            BookDocument d = new BookDocument();
            d.setId(id);
            when(h.source()).thenReturn(d);
            hitList.add(h);
        }

        when(hits.hits()).thenReturn(hitList);
        when(resp.hits()).thenReturn(hits);
        return resp;
    }

    @SuppressWarnings("unchecked")
    private static SearchResponse<BookDocument> mockSearchResponseMinimal(List<Long> ids) {
        SearchResponse<BookDocument> resp = mock(SearchResponse.class);
        HitsMetadata<BookDocument> hits = mock(HitsMetadata.class);

        List<Hit<BookDocument>> hitList = new ArrayList<>();
        for (Long id : ids) {
            Hit<BookDocument> h = mock(Hit.class);
            BookDocument d = new BookDocument();
            d.setId(id);
            when(h.source()).thenReturn(d);
            hitList.add(h);
        }

        when(hits.hits()).thenReturn(hitList);
        when(resp.hits()).thenReturn(hits);
        return resp;
    }
}
