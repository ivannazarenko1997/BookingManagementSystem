package com.example.bookstore.search.service ;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.util.ObjectBuilder;
import com.example.bookstore.dto.BookSearchDto;
import com.example.bookstore.search.dto.BookSearchItem;
import com.example.bookstore.search.model.BookDocument;
import com.example.bookstore.search.service.impl.BookSearchCustomServiceImpl;
import com.example.bookstore.service.BookService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;


@SpringBootTest(
        classes = { BookSearchCustomServiceImpl.class },
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@EnableAutoConfiguration(exclude = {
        ElasticsearchClientAutoConfiguration.class,
        ElasticsearchDataAutoConfiguration.class,
        ElasticsearchRestClientAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class
})
@Import(BookSearchCustomServiceImpl.class)
class BookSearchCustomServiceImplIT {

    @Autowired
    private BookSearchCustomServiceImpl bookSearchService;

    @MockBean
    private ElasticsearchClient elasticsearchClient;

    @MockBean
    private BookService bookService;


    private SearchResponse<BookDocument> buildSearchResponseWithIds(List<Long> ids, Long total) {
        String indexName = "books"; // must match service's INDEX_NAME
        List<Hit<BookDocument>> hits = ids.stream()
                .map(id -> new Hit.Builder<BookDocument>()
                        .index(indexName)                                 // REQUIRED
                        .id(String.valueOf(id))                           // good practice
                        .score(Double.valueOf(1.0f))                                      // optional
                        .source(new BookDocument(id, null, null, null, null))
                        .build())
                .toList();

        HitsMetadata<BookDocument> hitsMetadata = new HitsMetadata.Builder<BookDocument>()
                .hits(hits)
                .total(total != null
                        ? new TotalHits.Builder().value(total).relation(TotalHitsRelation.Eq).build()
                        : null)
                .build();

        return new SearchResponse.Builder<BookDocument>()
                .hits(hitsMetadata)
                .took(1)
                .timedOut(false)
                .shards(s -> s.total(1).successful(1).failed(0))
                .build();
    }

    @Test
    void searchBooks_hydratesIds_andReturnsSortedByPriceAsc() throws Exception {
        SearchResponse<BookDocument> esResponse = buildSearchResponseWithIds(List.of(3L, 1L, 2L), 3L);
        when(elasticsearchClient.search(any(Function.class), eq(BookDocument.class)))
                .thenReturn(esResponse);

        BookDocument bookDocument1 = new BookDocument(1L, "B", "Author", "Fantasy", new BigDecimal("20.00"));
        BookDocument bookDocument2 = new BookDocument(2L, "C", "Author", "Fantasy", new BigDecimal("10.00"));
        BookDocument bookDocument3 = new BookDocument(3L, "A", "Author", "Fantasy", new BigDecimal("15.00"));
        when(bookService.getDocumentsByIds(List.of(3L, 1L, 2L)))
                .thenReturn(List.of(bookDocument1, bookDocument2, bookDocument3));

        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("price")));
      //  Mockito.any()
        BookSearchDto filters = BookSearchDto.builder().q(null)
                .order(null).genre(null).title(null).minPrice(null).maxPrice(null).build();
        Page<BookSearchItem> searchResult = bookSearchService.searchBooks(filters, pageable);

        assertThat(searchResult.getContent())
                .extracting(BookSearchItem::getId)
                .containsExactly(2L, 3L, 1L);
        assertThat(searchResult.getTotalElements()).isEqualTo(3);

        verify(bookService).getDocumentsByIds(List.of(3L, 1L, 2L));
        verify(elasticsearchClient).search(any(Function.class), eq(BookDocument.class));
    }

    @Test
    void searchBooks_multiSort_titleDesc_thenIdAsc() throws Exception {
        SearchResponse<BookDocument> esResponse = buildSearchResponseWithIds(List.of(1L, 2L, 3L), 3L);
        when(elasticsearchClient.search(any(Function.class), eq(BookDocument.class)))
                .thenReturn(esResponse);


        BookDocument bookDocument1 = new BookDocument(1L, "Alpha", "Author", "Fantasy", new BigDecimal("10.00"));
        BookDocument bookDocument2 = new BookDocument(2L, "Alpha", "Author", "Fantasy", new BigDecimal("12.00"));
        BookDocument bookDocument3 = new BookDocument(3L, "Beta",  "Author", "Fantasy", new BigDecimal("11.00"));
        when(bookService.getDocumentsByIds(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(bookDocument1, bookDocument2, bookDocument3));

        PageRequest pageable = PageRequest.of(0, 10, Sort.by(
                Sort.Order.desc("title"),
                Sort.Order.asc("id")
        ));
        BookSearchDto filters = BookSearchDto.builder().q(null)
                .order(null).genre(null).title(null).minPrice(null).maxPrice(null).build();
        Page<BookSearchItem> searchResult = bookSearchService.searchBooks(
                filters, pageable);


        assertThat(searchResult.getContent())
                .extracting(BookSearchItem::getId)
                .containsExactly(3L, 1L, 2L);
    }

    @Test
    void searchBooks_emptyHits_returnsEmptyPage() throws Exception {

        SearchResponse<BookDocument> esResponse = buildSearchResponseWithIds(List.of(), null);
        when(elasticsearchClient.search(any(Function.class), eq(BookDocument.class)))
                .thenReturn(esResponse);

        BookSearchDto filters = BookSearchDto.builder().q("anything")
                .order(null).genre(null).title(null).minPrice(null).maxPrice(null).build();
        Page<BookSearchItem> searchResult = bookSearchService.searchBooks(
                filters, PageRequest.of(0, 5));


        assertThat(searchResult.getTotalElements()).isZero();
        assertThat(searchResult.getContent()).isEmpty();
        verify(bookService, never()).getDocumentsByIds(any());
    }

    @Test
    void searchBooks_handlesElasticsearchException_returnsEmptyPage() throws Exception {

        when(elasticsearchClient.search(any(Function.class), eq(BookDocument.class)))
                .thenThrow(new RuntimeException("ES down"));

        BookSearchDto filters = BookSearchDto.builder().q("q")
                . genre("a").title("t").minPrice(new BigDecimal("5")).maxPrice(new BigDecimal("50")).build();
        Page<BookSearchItem> searchResult = bookSearchService.searchBooks(
                filters, PageRequest.of(0, 10));

        assertThat(searchResult.getTotalElements()).isZero();
        assertThat(searchResult.getContent()).isEmpty();
        verify(bookService, never()).getDocumentsByIds(any());
    }

    @Test
    void searchBooks_buildsRequest_withExpectedIndexAndPaging() throws Exception {

        SearchResponse<BookDocument> esResponse = buildSearchResponseWithIds(List.of(1L), 1L);
        when(elasticsearchClient.search(any(Function.class), eq(BookDocument.class)))
                .thenReturn(esResponse);


        PageRequest pageable = PageRequest.of(2, 5, Sort.by(Sort.Order.asc("price")));

        BookSearchDto filters = BookSearchDto.builder().q("wizard")
                . genre("Rowling").title("Fantasy").minPrice(new BigDecimal("10")).maxPrice(new BigDecimal("40")).build();
        bookSearchService.searchBooks(filters, pageable);


        @SuppressWarnings("unchecked")
        ArgumentCaptor<Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>>> builderCaptor =
                ArgumentCaptor.forClass((Class) Function.class);

        verify(elasticsearchClient).search(builderCaptor.capture(), eq(BookDocument.class));

        SearchRequest.Builder requestBuilder = new SearchRequest.Builder();
        SearchRequest builtRequest = builderCaptor.getValue().apply(requestBuilder).build();

        assertThat(builtRequest.index()).containsExactly("books"); // must match INDEX_NAME in service
        assertThat(builtRequest.size()).isEqualTo(5);
        assertThat(builtRequest.from()).isEqualTo(10); // 2 * 5
    }
}
