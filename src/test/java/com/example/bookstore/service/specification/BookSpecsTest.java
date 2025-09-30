package com.example.bookstore.service.specification;

import com.example.bookstore.domain.Book;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BookSpecsTest {

    private Root<Book> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder cb;

    @BeforeEach
    void setup() {
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        cb = mock(CriteriaBuilder.class);
    }

    @Test
    void shouldCreateSpecificationWithTitleOnly() {
        Root<Book> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<String> titlePath = mock(Path.class);
        when(root.get("title")).thenReturn((Path) titlePath);
        when(cb.lower(titlePath)).thenReturn(titlePath);
        when(cb.like(titlePath, "%harry%")).thenReturn(mock(Predicate.class));
        when(cb.and(any())).thenReturn(mock(Predicate.class));

        Specification<Book> spec = BookSpecs.matches(null, "harry", null, null, null, null);
        Predicate predicate = spec.toPredicate(root, query, cb);

        assertThat(predicate).isNotNull();
    }
    @Test
    void shouldCreatePredicateWithTitleOnly() {
        Path<String> titlePath = mock(Path.class);
        when(root.get("title")).thenReturn((Path) titlePath);
        when(cb.lower(titlePath)).thenReturn(titlePath);
        when(cb.like(titlePath, "%harry%")).thenReturn(mock(Predicate.class));
        when(cb.and(any())).thenReturn(mock(Predicate.class));

        Specification<Book> spec = BookSpecs.matches(null, "harry", null, null, null, null);
        Predicate predicate = spec.toPredicate(root, query, cb);

        assertThat(predicate).isNotNull();
    }

    @Test
    void shouldReturnEmptyPredicateWhenAllInputsNull() {
        Predicate empty = mock(Predicate.class);
        when(cb.and()).thenReturn(empty);

        Specification<Book> spec = BookSpecs.matches(null, null, null, null, null, null);
        Predicate predicate = spec.toPredicate(root, query, cb);

        assertThat(predicate).isEqualTo(empty);
    }

    @Test
    void shouldReturnEmptyPredicateWhenAllInputsBlank() {
        Predicate empty = mock(Predicate.class);
        when(cb.and()).thenReturn(empty);

        Specification<Book> spec = BookSpecs.matches(" ", " ", " ", " ", null, null);
        Predicate predicate = spec.toPredicate(root, query, cb);

        assertThat(predicate).isEqualTo(empty);
    }

    @Test
    void shouldHandleGenreJoin() {
        Join<Object, Object> genreJoin = mock(Join.class);
        Path<String> genreName = mock(Path.class);

        when(root.join("genre")).thenReturn(genreJoin);
        when(genreJoin.get("name")).thenReturn((Path) genreName);
        when(cb.lower(genreName)).thenReturn(genreName);
        when(cb.like(genreName, "%genre%")).thenReturn(mock(Predicate.class));
        when(cb.and(any())).thenReturn(mock(Predicate.class));

        Specification<Book> spec = BookSpecs.matches(null, null, null, "genre", null, null);
        Predicate predicate = spec.toPredicate(root, query, cb);

        assertThat(predicate).isNotNull();
    }
}
