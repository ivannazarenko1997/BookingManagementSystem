package com.example.bookstore.controller;

import com.example.bookstore.search.dto.BookSearchItem;
import com.example.bookstore.search.service.BookSearchCustomService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookSearchCustomService bookSearchService;


    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Page<BookSearchItem> getBooks(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        System.out.println("getBooks");
        return bookSearchService.searchBooks(q, title, author, genre, minPrice, maxPrice, pageable);
    }
}
