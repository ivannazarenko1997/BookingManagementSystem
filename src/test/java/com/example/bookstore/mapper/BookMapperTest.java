package com.example.bookstore.mapper;

import com.example.bookstore.domain.Author;
import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.Genre;
import com.example.bookstore.dto.BookRequest;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.mappers.BookMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BookMapperTest {

    @Test
    void shouldMapBookToDto() {
        Author author = new Author();
        author.setId(1L);
        author.setName("Jane Austen");

        Genre genre = new Genre();
        genre.setId(2L);
        genre.setName("Classic");

        Book book = new Book();
        book.setId(10L);
        book.setTitle("Pride and Prejudice");
        book.setAuthor(author);
        book.setGenre(genre);
        book.setPrice(BigDecimal.valueOf(19.99));
        book.setCaption("A timeless romance");
        book.setDescription("A novel about manners and marriage.");
        book.setIsbn("1234567890");
        book.setPublishedYear(1813);
        book.setPublisher("Penguin");
        book.setPageCount(432);
        book.setLanguage("English");
        book.setStock(100);
        book.setCoverImageUrl("http://example.com/cover.jpg");

        BookResponse dto = BookMapper.toDto(book);

        assertThat(dto).isNotNull();
        assertThat(dto.getTitle()).isEqualTo("Pride and Prejudice");
        assertThat(dto.getAuthor().getName()).isEqualTo("Jane Austen");
        assertThat(dto.getGenre().getName()).isEqualTo("Classic");
        assertThat(dto.getIsbn()).isEqualTo("1234567890");
    }

    @Test
    void shouldReturnNullDtoWhenBookIsNull() {
        BookResponse dto = BookMapper.toDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void shouldMapRequestToEntity() {
        BookRequest req = BookRequest.builder()
                .title("1984")
                .price(BigDecimal.valueOf(14.99))
                .caption("Dystopian classic")
                .description("A novel about surveillance and control.")
                .isbn(" ")
                .publishedYear(1949)
                .publisher("Secker & Warburg")
                .pageCount(328)
                .language("English")
                .stock(50)
                .coverImageUrl("http://example.com/1984.jpg")
                .build();

        Author author = new Author();
        author.setId(3L);
        author.setName("George Orwell");

        Genre genre = new Genre();
        genre.setId(4L);
        genre.setName("Dystopian");

        Book entity = BookMapper.toEntity(req, author, genre);

        assertThat(entity.getTitle()).isEqualTo("1984");
        assertThat(entity.getAuthor().getName()).isEqualTo("George Orwell");
        assertThat(entity.getGenre().getName()).isEqualTo("Dystopian");
        assertThat(entity.getIsbn()).isNull(); // trimmed blank converted to null
    }

    @Test
    void shouldUpdateExistingEntity() {
        BookRequest req = BookRequest.builder()
                .title("Updated Title")
                .price(BigDecimal.valueOf(9.99))
                .caption("Updated Caption")
                .description("Updated Description")
                .isbn("9876543210")
                .publishedYear(2020)
                .publisher("Updated Publisher")
                .pageCount(200)
                .language("French")
                .stock(25)
                .coverImageUrl("http://example.com/updated.jpg")
                .build();

        Author author = new Author();
        author.setId(5L);
        author.setName("Updated Author");

        Genre genre = new Genre();
        genre.setId(6L);
        genre.setName("Updated Genre");

        Book target = new Book();
        target.setTitle("Old Title");

        BookMapper.updateEntity(target, req, author, genre);

        assertThat(target.getTitle()).isEqualTo("Updated Title");
        assertThat(target.getAuthor().getName()).isEqualTo("Updated Author");
        assertThat(target.getGenre().getName()).isEqualTo("Updated Genre");
        assertThat(target.getIsbn()).isEqualTo("9876543210");
    }


}
