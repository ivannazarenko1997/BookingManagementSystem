package com.example.bookstore.search.dto;


import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@ToString
@Data

@Builder
public class BookSearchItem {
    private Long id;
    private String title;
    private String authorName;
    private String genreName;
    private BigDecimal price;
}