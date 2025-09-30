package com.example.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {
    private Long id;
    private String title;
    private AuthorResponse author;
    private GenreResponse genre;
    private BigDecimal price;

    private String caption;
    private String description;
    private String isbn;
    private Integer publishedYear;
    private String publisher;
    private Integer pageCount;
    private String language;
    private Integer stock;
    private String coverImageUrl;
    private Instant createdAt;
    private Instant updatedAt;
}