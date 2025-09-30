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

    public static Book toEntity(BookRequest request, Author author, Genre genre) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(author);
        book.setGenre(genre);
        book.setPrice(request.getPrice());
        book.setCaption(request.getCaption());
        book.setDescription(request.getDescription());
        book.setIsbn(nvlBlankToNull(request.getIsbn()));
        book.setPublishedYear(request.getPublishedYear());
        book.setPublisher(request.getPublisher());
        book.setPageCount(request.getPageCount());
        book.setLanguage(request.getLanguage());
        if (request.getStock() != null) {
            book.setStock(request.getStock());
        }
        book.setCoverImageUrl(request.getCoverImageUrl());
        return book;
    }

    public static void updateEntity(Book target, BookRequest updateRequest, Author author, Genre genre) {
        target.setTitle(updateRequest.getTitle());
        target.setAuthor(author);
        target.setGenre(genre);
        target.setPrice(updateRequest.getPrice());

        target.setCaption(updateRequest.getCaption());
        target.setDescription(updateRequest.getDescription());
        target.setIsbn(nvlBlankToNull(updateRequest.getIsbn()));
        target.setPublishedYear(updateRequest.getPublishedYear());
        target.setPublisher(updateRequest.getPublisher());
        target.setPageCount(updateRequest.getPageCount());
        target.setLanguage(updateRequest.getLanguage());
        if (updateRequest.getStock() != null) {
            target.setStock(updateRequest.getStock());
        }
        target.setCoverImageUrl(updateRequest.getCoverImageUrl());
    }

    private static String nvlBlankToNull(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

}