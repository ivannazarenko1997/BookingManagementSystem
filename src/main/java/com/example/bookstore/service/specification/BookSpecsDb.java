package com.example.bookstore.service.specification;

import com.example.bookstore.domain.Book;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;



public final class BookSpecsDb {
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_AUTHOR = "author";
    private static final String FIELD_GENRE = "genre";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PRICE = "price";

    private BookSpecsDb() {  }

    public static Specification<Book> likeTitle(String title) {
        return (root, q, cb) -> !isExists(title) ? null :
                cb.like(cb.lower(root.get(FIELD_TITLE)), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Book> likeAuthor(String author) {
        return (root, q, cb) -> !isExists(author) ? null :
                cb.like(cb.lower(root.join(FIELD_AUTHOR).get(FIELD_NAME)), "%" + author.toLowerCase() + "%");
    }

    public static Specification<Book> likeGenre(String genre) {
        return (root, q, cb) -> !isExists(genre) ? null :
                cb.like(cb.lower(root.join(FIELD_GENRE).get(FIELD_NAME)), "%" + genre.toLowerCase() + "%");
    }

    public static Specification<Book> fullText(String qstr) {
        if (!isExists(qstr))
            return (root, q, cb) -> null;

        String value = "%" + qstr.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get(FIELD_TITLE)), value),
                cb.like(cb.lower(root.join(FIELD_AUTHOR).get(FIELD_NAME)), value),
                cb.like(cb.lower(root.join(FIELD_GENRE).get(FIELD_NAME)), value)
        );
    }

    public static Specification<Book> priceGte(BigDecimal min) {
        return (root, q, cb) -> !isExists(min) ? null :
                cb.greaterThanOrEqualTo(root.get(FIELD_PRICE), min);
    }

    public static Specification<Book> priceLte(BigDecimal max) {
        return (root, q, cb) -> !isExists(max) ? null :
                cb.lessThanOrEqualTo(root.get(FIELD_PRICE), max);
    }

    public static boolean isExists(String check) {
        return StringUtils.hasText(check);
    }

    public static boolean isExists(BigDecimal check) {
        return (check != null);
    }

}
