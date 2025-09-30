package com.example.bookstore.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;


@Data
@Entity
@Table(name = "books", indexes = {
        @Index(name = "idx_books_title", columnList = "title"),
        @Index(name = "idx_books_isbn", columnList = "isbn")
})
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank
    @Size(max = 512)
    private String title;


    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;


    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id")
    private Genre genre;


    @Column(nullable = false, precision = 10, scale = 2)
    @Positive
    private BigDecimal price;


    @Column(length = 255)
    private String caption;


    @Column(columnDefinition = "text")
    private String description;


    @Column(length = 20, unique = true)
    private String isbn;


    @Column(name = "published_year")
    private Integer publishedYear;


    @Column(length = 255)
    private String publisher;


    @Column(name = "page_count")
    private Integer pageCount;


    @Column(length = 10)
    private String language;


    @Column(nullable = false)
    private Integer stock = 0;


    @Column(length = 1024)
    private String coverImageUrl;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;


    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;


}