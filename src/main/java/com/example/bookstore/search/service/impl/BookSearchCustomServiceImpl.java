package com.example.bookstore.search.service.impl;

import static com.example.bookstore.service.specification.BookSpecsDb.fullText;
import static com.example.bookstore.service.specification.BookSpecsDb.likeAuthor;
import static com.example.bookstore.service.specification.BookSpecsDb.likeGenre;
import static com.example.bookstore.service.specification.BookSpecsDb.likeTitle;
import static com.example.bookstore.service.specification.BookSpecsDb.priceGte;
import static com.example.bookstore.service.specification.BookSpecsDb.priceLte;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.example.bookstore.domain.Book;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.dto.BookSearchDto;
import com.example.bookstore.mappers.BookMapper;
import com.example.bookstore.search.dto.BookSearchItem;
import com.example.bookstore.search.mapper.BookDocumentMapper;
import com.example.bookstore.search.model.BookDocument;
import com.example.bookstore.search.service.BookSearchCustomService;
import com.example.bookstore.service.BookService;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private static final String TITLE = "title";
    private static final String AUTHOR = "authorName";
    private static final String GERNE = "genreName";
    private static final String PRICE = "price";
    private static final String ID = "id";

    private final ElasticsearchClient elasticsearchClient;
    private final BookService bookService;


    public BookSearchCustomServiceImpl(ElasticsearchClient elasticsearchClient,
                                       BookService bookService) {
        this.elasticsearchClient = elasticsearchClient;
        this.bookService = bookService;

    }

    @Override
    @Timed(
            value = "books.search.es.timer",
            description = "Time to execute a book search",
            extraTags = {"component", "booking-service"}
    )
    public Page<BookSearchItem> searchBooks(BookSearchDto filters,
                                            Pageable pageable) {
        try {
            Query query = buildSearchQuery(filters);
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
            log.error("Search books in elasticSearch failed", ex);
            return new PageImpl<>(List.of(), pageable, 0);
        }
    }


    @Override
    @Transactional(readOnly = true)
    @Timed(
            value = "books.search.db.timer",
            description = "Time to execute a book search",
            extraTags = {"component", "booking-service"}
    )
    public Page<BookResponse> searchDb(
            BookSearchDto dto, Pageable pageable) {
        try {
            Specification<Book> spec = Specification.<Book>where(null)
                    .and(likeTitle(dto.getTitle()))
                    .and(likeAuthor(dto.getAuthor()))
                    .and(likeGenre(dto.getGenre()))
                    .and(fullText(dto.getQ()))
                    .and(priceGte(dto.getMinPrice()))
                    .and(priceLte(dto.getMaxPrice()));

            if (StringUtils.hasText(dto.getOrder())) {
                Sort.Direction dir = "desc".equalsIgnoreCase(dto.getOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
                pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(dir, "price"));
            }

            return bookService.findAll(pageable,spec)
                    .map(BookMapper::toDto);
        } catch (Exception ex) {
            log.error("Search books in DB failed", ex);
            return new PageImpl<>(List.of(), pageable, 0);
        }
    }

    private SearchResponse<BookDocument> executeSearch(Query query, Pageable pageable) throws Exception {
        return elasticsearchClient.search(
                s -> s.index(INDEX_NAME)
                        .from(pageable.getPageNumber() * pageable.getPageSize())
                        .size(pageable.getPageSize())
                        .trackTotalHits(t -> t.enabled(true))
                        .source(src -> src.filter(f -> f.includes(ID)))
                        .query(query),
                BookDocument.class
        );
    }

    private static String safeLower(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
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

    private Query buildSearchQuery(BookSearchDto filters) {
        BoolQuery.Builder bool = new BoolQuery.Builder();

        if (isExists(filters.getQ())) {
            List<Query> should = new ArrayList<>();
            should.add(Query.of(b -> b.match(createMatchQuery(TITLE, filters.getQ()))));
            should.add(Query.of(b -> b.match(createMatchQuery(AUTHOR, filters.getQ()))));
            should.add(Query.of(b -> b.match(createMatchQuery(GERNE, filters.getQ()))));
            bool.should(should).minimumShouldMatch("1");
        }

        if (isExists(filters.getTitle())) {
            bool.must(Query.of(b -> b.match(createMatchQuery(TITLE, filters.getTitle()))));
        }
        if (isExists(filters.getAuthor())) {
            bool.must(Query.of(b -> b.match(createMatchQuery(AUTHOR, filters.getAuthor()))));
        }
        if (isExists(filters.getGenre())) {
            bool.must(Query.of(b -> b.match(createMatchQuery(GERNE, filters.getGenre()))));
        }

        if (isExists(filters.getMinPrice())|| isExists(filters.getMaxPrice())) {
            RangeQuery.Builder range = new RangeQuery.Builder().field(PRICE);
            if (filters.getMinPrice() != null) {
                range.gte(JsonData.of(filters.getMinPrice()));
            }
            if (filters.getMaxPrice() != null) {
                range.lte(JsonData.of(filters.getMaxPrice()));
            }
            bool.must(Query.of(b -> b.range(range.build())));
        }

        return Query.of(qb -> qb.bool(bool.build()));
    }
    public static boolean isExists(String check) {
        return StringUtils.hasText(check);
    }

    public static boolean isExists(BigDecimal check) {
        return (check != null);
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
                case PRICE -> Comparator.comparing(
                        BookDocument::getPrice,
                        Comparator.nullsLast(BigDecimal::compareTo)
                );
                case TITLE -> Comparator.comparing(
                        b -> safeLower(b.getTitle()),
                        Comparator.nullsLast(String::compareTo)
                );
                case AUTHOR -> Comparator.comparing(
                        b -> safeLower(b.getAuthorName()),
                        Comparator.nullsLast(String::compareTo)
                );
                case GERNE -> Comparator.comparing(
                        b -> safeLower(b.getGenreName()),
                        Comparator.nullsLast(String::compareTo)
                );
                case ID -> Comparator.comparing(
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
