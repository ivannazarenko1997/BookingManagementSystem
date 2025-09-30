package com.example.bookstore.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.example.bookstore.search.dto.BookSearchItem;
import com.example.bookstore.search.mapper.BookDocumentMapper;
import com.example.bookstore.search.model.BookDocument;
import com.example.bookstore.search.service.BookSearchCustomService;
import com.example.bookstore.service.BookService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
public class BookSearchCustomServiceImpl implements BookSearchCustomService {

    private static final String INDEX_NAME = "books";

    private final ElasticsearchClient elasticsearchClient;
    private final BookService bookService;
    private final MeterRegistry meterRegistry;
    private Counter createCounter;
    private Counter updateCounter;

    public BookSearchCustomServiceImpl(ElasticsearchClient elasticsearchClient,
                                       BookService bookService,
                                       MeterRegistry meterRegistry) {
        this.elasticsearchClient = elasticsearchClient;
        this.bookService = bookService;
        this.meterRegistry =meterRegistry;
        this.createCounter = Counter.builder("books.operations.total")
                .description("Total created books")
                .tag("operation", "create")
                .tag("component", "service")
                .register(meterRegistry);

        this.updateCounter = Counter.builder("books.operations.total")
                .description("Total updated books")
                .tag("operation", "update")
                .tag("component", "service")
                .register(meterRegistry);
    }
    private static String safeLower(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    @Override
    public Page<BookSearchItem> searchBooks(String queryText,
                                            String title,
                                            String author,
                                            String genre,
                                            BigDecimal minPrice,
                                            BigDecimal maxPrice,
                                            Pageable pageable) {
        try {
            Query query = buildSearchQuery(queryText, title, author, genre, minPrice, maxPrice);
            SearchResponse<BookDocument> response = executeSearch(query, pageable);

            List<BookDocument> documents = hydrateDocuments(response);
            long totalHits = response.hits().total() != null
                    ? response.hits().total().value()
                    : documents.size();

            List<BookDocument> sorted = applySorting(documents, pageable.getSort());
            List<BookSearchItem> result = sorted.stream()
                    .map(BookDocumentMapper::toSearchItem)
                    .toList();

            return new PageImpl<>(result, pageable, totalHits);
        } catch (Exception ex) {
            log.error("Search failed", ex);
            return new PageImpl<>(List.of(), pageable, 0);
        }
    }

    private SearchResponse<BookDocument> executeSearch(Query query, Pageable pageable) throws Exception {
        return elasticsearchClient.search(
                s -> s.index(INDEX_NAME)
                        .from(pageable.getPageNumber() * pageable.getPageSize())
                        .size(pageable.getPageSize())
                        .trackTotalHits(t -> t.enabled(true))
                        .source(src -> src.filter(f -> f.includes("id")))
                        .query(query),
                BookDocument.class
        );
    }

    private List<BookDocument> hydrateDocuments(SearchResponse<BookDocument> response) {
        List<Long> ids = response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .map(BookDocument::getId)
                .filter(Objects::nonNull)
                .toList();

        return ids.isEmpty() ? List.of() : bookService.getDocumentsByIds(ids);
    }

    private Query buildSearchQuery(String queryText,
                                   String title,
                                   String author,
                                   String genre,
                                   BigDecimal minPrice,
                                   BigDecimal maxPrice) {
        BoolQuery.Builder bool = new BoolQuery.Builder();

        if (StringUtils.hasText(queryText)) {
            List<Query> should = new ArrayList<>();
            should.add(Query.of(b -> b.match(createMatchQuery("title", queryText))));
            should.add(Query.of(b -> b.match(createMatchQuery("authorName", queryText))));
            should.add(Query.of(b -> b.match(createMatchQuery("genreName", queryText))));
            bool.should(should).minimumShouldMatch("1");
        }

        if (StringUtils.hasText(title)) {
            bool.must(Query.of(b -> b.match(createMatchQuery("title", title))));
        }
        if (StringUtils.hasText(author)) {
            bool.must(Query.of(b -> b.match(createMatchQuery("authorName", author))));
        }
        if (StringUtils.hasText(genre)) {
            bool.must(Query.of(b -> b.match(createMatchQuery("genreName", genre))));
        }

        if (minPrice != null || maxPrice != null) {
            RangeQuery.Builder range = new RangeQuery.Builder().field("price");
            if (minPrice != null)
                range.gte(JsonData.of(minPrice));
            if (maxPrice != null)
                range.lte(JsonData.of(maxPrice));
            bool.must(Query.of(b -> b.range(range.build())));
        }

        return Query.of(qb -> qb.bool(bool.build()));
    }

    private MatchQuery createMatchQuery(String field, String value) {
        return new MatchQuery.Builder()
                .field(field)
                .query(value)
                .build();
    }

    private List<BookDocument> applySorting(List<BookDocument> documents, Sort sort) {
        if (documents == null || documents.size() <= 1 || sort == null || sort.isUnsorted()) {
            return documents;
        }

        Comparator<BookDocument> comparator = buildComparator(sort);
        if (comparator == null) {
            return documents;
        }

        List<BookDocument> copy = new ArrayList<>(documents);
        copy.sort(Comparator.nullsLast(comparator));
        return copy;
    }

    private Comparator<BookDocument> buildComparator(Sort sort) {
        Comparator<BookDocument> comparator = null;

        for (Sort.Order order : sort) {
            String property = order.getProperty().toLowerCase(Locale.ROOT);
            boolean ascending = order.isAscending();

            Comparator<BookDocument> next = switch (property) {
                case "price" -> Comparator.comparing(
                        BookDocument::getPrice,
                        Comparator.nullsLast(BigDecimal::compareTo)
                );
                case "title" -> Comparator.comparing(
                        b -> safeLower(b.getTitle()),
                        Comparator.nullsLast(String::compareTo)
                );
                case "authorname" -> Comparator.comparing(
                        b -> safeLower(b.getAuthorName()),
                        Comparator.nullsLast(String::compareTo)
                );
                case "genrename" -> Comparator.comparing(
                        b -> safeLower(b.getGenreName()),
                        Comparator.nullsLast(String::compareTo)
                );
                case "id" -> Comparator.comparing(
                        BookDocument::getId,
                        Comparator.nullsLast(Long::compareTo)
                );
                default -> null;
            };

            if (next == null)
                continue;
            if (!ascending)
                next = next.reversed();
            comparator = (comparator == null) ? next : comparator.thenComparing(next);
        }

        if (comparator != null) {
            comparator = comparator.thenComparing(
                    Comparator.comparing(BookDocument::getId, Comparator.nullsLast(Long::compareTo))
            );
        }
        return comparator;
    }
}
