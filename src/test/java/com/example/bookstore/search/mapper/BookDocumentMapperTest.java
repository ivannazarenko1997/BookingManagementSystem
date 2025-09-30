package com.example.bookstore.search.mapper;


import com.example.bookstore.domain.Author;
import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.BookIndexProjection;
import com.example.bookstore.domain.Genre;
import com.example.bookstore.search.dto.BookSearchItem;
import com.example.bookstore.search.model.BookDocument;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BookDocumentMapperTest {

    @Test
    void shouldReturnNullForNullProjection() {
        BookDocument document = BookDocumentMapper.toDocument((BookIndexProjection) null);
        assertThat(document).isNull();
    }

    @Test
    void shouldMapBookToDocument() {
        Author author = new Author();
        author.setName("George Orwell");

        Genre genre = new Genre();
        genre.setName("Dystopian");

        Book book = new Book();
        book.setId(2L);
        book.setTitle("1984");
        book.setAuthor(author);
        book.setGenre(genre);
        book.setPrice(BigDecimal.valueOf(19.84));

        BookDocument document = BookDocumentMapper.toDocument(book);

        assertThat(document).isNotNull();
        assertThat(document.getId()).isEqualTo(2L);
        assertThat(document.getTitle()).isEqualTo("1984");
        assertThat(document.getAuthorName()).isEqualTo("George Orwell");
        assertThat(document.getGenreName()).isEqualTo("Dystopian");
        assertThat(document.getPrice()).isEqualTo(BigDecimal.valueOf(19.84));
    }

    @Test
    void shouldReturnNullForNullBook() {
        BookDocument document = BookDocumentMapper.toDocument((Book) null);
        assertThat(document).isNull();
    }

    @Test
    void shouldMapDocumentToSearchItem() {
        BookDocument document = new BookDocument();
        document.setId(3L);
        document.setTitle("Clean Code");
        document.setAuthorName("Robert C. Martin");
        document.setGenreName("Programming");
        document.setPrice(BigDecimal.valueOf(45.00));

        BookSearchItem item = BookDocumentMapper.toSearchItem(document);

        assertThat(item).isNotNull();
        assertThat(item.getId()).isEqualTo(3L);
        assertThat(item.getTitle()).isEqualTo("Clean Code");
        assertThat(item.getAuthorName()).isEqualTo("Robert C. Martin");
        assertThat(item.getGenreName()).isEqualTo("Programming");
        assertThat(item.getPrice()).isEqualTo(BigDecimal.valueOf(45.00));
    }

    @Test
    void shouldReturnNullForNullDocument() {
        BookSearchItem item = BookDocumentMapper.toSearchItem(null);
        assertThat(item).isNull();
    }

    @Test
    void shouldHandleBookWithNullAuthorAndGenre() {
        Book book = new Book();
        book.setId(4L);
        book.setTitle("Nameless");
        book.setAuthor(null);
        book.setGenre(null);
        book.setPrice(BigDecimal.valueOf(10.00));

        BookDocument document = BookDocumentMapper.toDocument(book);

        assertThat(document).isNotNull();
        assertThat(document.getAuthorName()).isNull();
        assertThat(document.getGenreName()).isNull();
    }
}
