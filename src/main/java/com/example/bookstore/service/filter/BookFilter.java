package com.example.bookstore.service.filter;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
public class BookFilter {

    private final String query;
    private final String title;
    private final String author;
    private final String genre;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;

}