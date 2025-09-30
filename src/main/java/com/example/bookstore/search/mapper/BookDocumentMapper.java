package com.example.bookstore.search.mapper;

import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.BookIndexProjection;
import com.example.bookstore.search.dto.BookSearchItem;
import com.example.bookstore.search.model.BookDocument;

public final class BookDocumentMapper {

    private BookDocumentMapper() {
    }

    public static BookDocument toDocument(BookIndexProjection projection) {
        if (projection == null) {
            return null;
        }
        BookDocument document = new BookDocument();
        document.setId(projection.getId());
        document.setTitle(projection.getTitle());
        document.setAuthorName(projection.getAuthorName());
        document.setGenreName(projection.getGenreName());
        document.setPrice(projection.getPrice());
        return document;
    }

    public static BookSearchItem toSearchItem(BookDocument document) {
        if (document == null) {
            return null;
        }
        return
                BookSearchItem.builder()
                        .id(document.getId())
                        .title(document.getTitle())
                        .authorName(document.getAuthorName())
                        .genreName(document.getGenreName())
                        .price(document.getPrice()).build();
    }

    public static BookDocument toDocument(Book book) {
        if (book == null) {
            return null;
        }
        BookDocument document = new BookDocument();
        document.setId(book.getId());
        document.setTitle(book.getTitle());
        document.setAuthorName(book.getAuthor() != null ? book.getAuthor().getName() : null);
        document.setGenreName(book.getGenre() != null ? book.getGenre().getName() : null);
        document.setPrice(book.getPrice());
        return document;
    }
}
