package com.example.bookstore.service.specification;

import com.example.bookstore.domain.Book;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class BookSpecs {
    public static Specification<Book> matches(String q, String title, String author, String genre,
                                              BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.join("author").get("name")), like),
                        cb.like(cb.lower(root.join("genre").get("name")), like)
                ));
            }
            if (title != null && !title.isBlank())
                ps.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            if (author != null && !author.isBlank())
                ps.add(cb.like(cb.lower(root.join("author").get("name")), "%" + author.toLowerCase() + "%"));
            if (genre != null && !genre.isBlank())
                ps.add(cb.like(cb.lower(root.join("genre").get("name")), "%" + genre.toLowerCase() + "%"));
            if (minPrice != null)
                ps.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            if (maxPrice != null)
                ps.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}