package com.example.bookstore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BookRequest {
    @NotBlank
    private String title;
    @NotNull
    private Long authorId;
    @NotNull
    private Long genreId;
    @NotNull
    @Min(1)
    private BigDecimal price;

    @Size(max = 255)
    private String caption;
    private String description;
    @Size(max = 20)
    private String isbn;
    private Integer publishedYear;
    @Size(max = 255)
    private String publisher;
    private Integer pageCount;
    @Size(max = 10)
    private String language;
    private Integer stock;
    @Size(max = 1024)
    private String coverImageUrl;

}