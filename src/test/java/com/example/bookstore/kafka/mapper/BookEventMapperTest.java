package com.example.bookstore.kafka.mapper;

import com.example.bookstore.domain.Author;
import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.Genre;
import com.example.bookstore.kafka.event.BookEvent;
import com.example.bookstore.search.model.BookDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BookEventMapperTest {

    @Test
    @DisplayName("toBookEvent(entity) maps all fields")
    void toBookEventFromEntity() {
        Author author = new Author();
        author.setName("Eric Evans");

        Genre genre = new Genre();
        genre.setName("Design");

        Book book = new Book();
        book.setId(10L);
        book.setTitle("Domain-Driven Design");
        book.setAuthor(author);
        book.setGenre(genre);
        book.setPrice(new BigDecimal("55.99"));
        book.setCaption("classic");

        BookEvent event = BookEventMapper.toBookEvent("create", book);

        assertEquals(10L, event.getId());
        assertEquals("create", event.getType());
        assertEquals("Domain-Driven Design", event.getTitle());
        assertEquals("Eric Evans", event.getAuthorName());
        assertEquals("Design", event.getGenreName());
        assertEquals(0, new BigDecimal("55.99").compareTo(event.getPrice()));
        assertEquals("classic", event.getCaption());
    }

    @Test
    @DisplayName("toBookEvent(id) maps id and type only")
    void toBookEventFromId() {
        BookEvent event = BookEventMapper.toBookEvent("delete", 5L);

        assertEquals(5L, event.getId());
        assertEquals("delete", event.getType());
        assertNull(event.getTitle());
        assertNull(event.getAuthorName());
        assertNull(event.getGenreName());
        assertNull(event.getPrice());
        assertNull(event.getCaption());
    }

    @Test
    @DisplayName("toDocument(null) returns null")
    void toDocumentNullSafe() {
        assertNull(BookEventMapper.toDocument(null));
    }

    @Test
    @DisplayName("toDocument(event) maps selected fields")
    void toDocumentMapsFields() {
        BookEvent event = BookEvent.builder()
                .id(7L)
                .type("update")
                .title("Clean Code")
                .authorName("Robert C. Martin")
                .genreName("Programming")
                .price(new BigDecimal("39.00"))
                .caption("ignored-by-mapper")
                .build();

        BookDocument doc = BookEventMapper.toDocument(event);

        assertNotNull(doc);
        assertEquals(7L, doc.getId());
        assertEquals("Clean Code", doc.getTitle());
        assertEquals("Robert C. Martin", doc.getAuthorName());
        assertEquals("Programming", doc.getGenreName());
        assertEquals(0, new BigDecimal("39.00").compareTo(doc.getPrice()));
    }
}
