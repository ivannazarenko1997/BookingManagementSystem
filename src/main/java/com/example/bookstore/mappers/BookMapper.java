package com.example.bookstore.mappers;

import com.example.bookstore.domain.Author;
import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.Genre;
import com.example.bookstore.dto.AuthorResponse;
import com.example.bookstore.dto.BookRequest;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.dto.GenreResponse;

public final class BookMapper {
    private BookMapper() {
    }

    public static BookResponse toDto(Book book) {
        if (book == null) {
            return null;
        }

        Author author = book.getAuthor();
        Genre genre = book.getGenre();

        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .price(book.getPrice())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .caption(book.getCaption())
                .description(book.getDescription())
                .isbn(book.getIsbn())
                .publishedYear(book.getPublishedYear())
                .publisher(book.getPublisher())
                .pageCount(book.getPageCount())
                .language(book.getLanguage())
                .stock(book.getStock())
                .coverImageUrl(book.getCoverImageUrl())
                .author(author == null ? null :
                        AuthorResponse.builder()
                                .id(author.getId())
                                .name(author.getName())
                                .build())
                .genre(genre == null ? null :
                        GenreResponse.builder()
                                .id(genre.getId())
                                .name(genre.getName())
                                .build())
                .build();
    }

    public static Book toEntity(BookRequest req, Author author, Genre genre) {
        Book b = new Book();
        b.setTitle(req.getTitle());
        b.setAuthor(author);
        b.setGenre(genre);
        b.setPrice(req.getPrice());
        b.setCaption(req.getCaption());
        b.setDescription(req.getDescription());
        b.setIsbn(nvlBlankToNull(req.getIsbn()));
        b.setPublishedYear(req.getPublishedYear());
        b.setPublisher(req.getPublisher());
        b.setPageCount(req.getPageCount());
        b.setLanguage(req.getLanguage());
        if (req.getStock() != null) {
            b.setStock(req.getStock());
        }
        b.setCoverImageUrl(req.getCoverImageUrl());
        return b;
    }

    public static void updateEntity(Book target, BookRequest req, Author author, Genre genre) {
        target.setTitle(req.getTitle());
        target.setAuthor(author);
        target.setGenre(genre);
        target.setPrice(req.getPrice());

        target.setCaption(req.getCaption());
        target.setDescription(req.getDescription());
        target.setIsbn(nvlBlankToNull(req.getIsbn()));
        target.setPublishedYear(req.getPublishedYear());
        target.setPublisher(req.getPublisher());
        target.setPageCount(req.getPageCount());
        target.setLanguage(req.getLanguage());
        if (req.getStock() != null) {
            target.setStock(req.getStock());
        }
        target.setCoverImageUrl(req.getCoverImageUrl());
    }

    private static String nvlBlankToNull(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

}