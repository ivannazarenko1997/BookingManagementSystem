package com.example.bookstore.search.service;


import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.dto.BookSearchDto;
import com.example.bookstore.search.dto.BookSearchItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface BookSearchCustomService {

    Page<BookSearchItem> searchBooks(BookSearchDto filters,
                                     Pageable pageable);
    Page<BookResponse> searchDb(BookSearchDto filters, Pageable pageable);

}