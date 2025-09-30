package com.example.bookstore.search.repository;

import com.example.bookstore.search.model.BookDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataElasticsearchTest
class BookSearchRepositoryTest {

    @Autowired
    private BookSearchRepository bookSearchRepository;

    @BeforeEach
    void cleanIndex() {
        bookSearchRepository.deleteAll();
    }

    @Test
    void shouldSaveAndRetrieveBooksWithPagination() {
        // Given
        BookDocument book1 = new BookDocument();
        book1.setId(1L);
        book1.setTitle("Spring in Action");
        book1.setAuthorName("Craig Walls");
        book1.setGenreName("Programming");
        book1.setPrice(BigDecimal.valueOf(39.99));

        BookDocument book2 = new BookDocument();
        book2.setId(2L);
        book2.setTitle("Elasticsearch Basics");
        book2.setAuthorName("John Smith");
        book2.setGenreName("Search");
        book2.setPrice(BigDecimal.valueOf(29.99));

        bookSearchRepository.saveAll(List.of(book1, book2));

        Page<BookDocument> result = bookSearchRepository.findAll(PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        List<String> titles = result.getContent().stream()
                .map(BookDocument::getTitle)
                .toList();

        assertThat(titles).containsExactlyInAnyOrder("Spring in Action", "Elasticsearch Basics");
    }

    @Test
    void shouldReturnEmptyPageWhenNoBooksExist() {
        Page<BookDocument> result = bookSearchRepository.findAll(PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void shouldSupportPaginationLimits() {
        for (long i = 1; i <= 15; i++) {
            BookDocument book = new BookDocument();
            book.setId(i);
            book.setTitle("Book " + i);
            book.setAuthorName("Author " + i);
            book.setGenreName("Genre");
            book.setPrice(BigDecimal.valueOf(10 + i));
            bookSearchRepository.save(book);
        }

        Page<BookDocument> page1 = bookSearchRepository.findAll(PageRequest.of(0, 5));
        Page<BookDocument> page2 = bookSearchRepository.findAll(PageRequest.of(1, 5));

        assertThat(page1.getContent()).hasSize(5);
        assertThat(page2.getContent()).hasSize(5);
        assertThat(page1.getTotalElements()).isEqualTo(15);
    }
}
