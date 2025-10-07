package com.example.bookstore.controller;

import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.dto.BookSearchDto;
import com.example.bookstore.search.dto.BookSearchItem;
import com.example.bookstore.search.service.BookSearchCustomService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookSearchCustomService bookSearchService;

    @GetMapping(value = "/db",produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Page<BookResponse> search(
            @ModelAttribute BookSearchDto filters,
            @PageableDefault(size = 10) Pageable pageable) {
        return bookSearchService.searchDb(filters, pageable);
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Page<BookSearchItem> getBooks(
            @ParameterObject Pageable pageable,
            @ModelAttribute BookSearchDto filters ) {
        return bookSearchService.searchBooks(filters, pageable);
    }
}
