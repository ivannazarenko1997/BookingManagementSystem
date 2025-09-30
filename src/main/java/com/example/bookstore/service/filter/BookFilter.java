package com.example.bookstore.service.filter;

import java.math.BigDecimal;


public class BookFilter {

    private final String query;
    private final String title;
    private final String author;
    private final String genre;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;

    public BookFilter(String query, String title, String author, String genre, BigDecimal minPrice, BigDecimal maxPrice) {
        this.query = query;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public String getQuery() {
        return query;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }
}