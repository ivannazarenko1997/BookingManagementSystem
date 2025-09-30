package com.example.bookstore.search.service;


import com.example.bookstore.search.dto.BookSearchItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface BookSearchCustomService {

    Page<BookSearchItem> searchBooks(String q,
                                     String title,
                                     String author,
                                     String genre,
                                     BigDecimal minPrice,
                                     BigDecimal maxPrice,
                                     Pageable pageable);

}