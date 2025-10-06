package com.example.bookstore.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BookSearchDto {
    private String q;
    private String title;
    private String author;
    private String genre;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String order;
}
