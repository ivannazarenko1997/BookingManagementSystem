package com.example.bookstore.service.specification;

import com.example.bookstore.domain.Book;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public final class BookSpecs {
    private BookSpecs() {

    }

    public static Specification<Book> matches(String q, String title, String author, String genre,
                                              BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> searchConditions = new ArrayList<>();

            if (StringUtils.hasText(q)) {
                String like = "%" + q.toLowerCase() + "%";
                searchConditions.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.join("author").get("name")), like),
                        cb.like(cb.lower(root.join("genre").get("name")), like)
                ));
            }

            if (StringUtils.hasText(title)) {
                searchConditions.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }

            if (StringUtils.hasText(author)) {
                searchConditions.add(cb.like(cb.lower(root.join("author").get("name")),
                        "%" + author.toLowerCase() + "%"));
            }

            if (StringUtils.hasText(genre)) {
                searchConditions.add(cb.like(cb.lower(root.join("genre").get("name")),
                        "%" + genre.toLowerCase() + "%"));
            }

            if (minPrice != null) {
                searchConditions.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                searchConditions.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(searchConditions.toArray(new Predicate[0]));
        };
    }

}