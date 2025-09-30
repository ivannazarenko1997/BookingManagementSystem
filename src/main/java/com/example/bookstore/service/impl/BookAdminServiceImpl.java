package com.example.bookstore.service.impl;

import com.example.bookstore.domain.Author;
import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.Genre;
import com.example.bookstore.dto.BookRequest;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.exception.BookStoreException;
import com.example.bookstore.kafka.event.BookEvent;
import com.example.bookstore.kafka.event.BookEventType;
import com.example.bookstore.kafka.mapper.BookEventMapper;
import com.example.bookstore.kafka.producer.BookEventPublisher;
import com.example.bookstore.mappers.BookMapper;
import com.example.bookstore.service.AuthorService;
import com.example.bookstore.service.BookAdminService;
import com.example.bookstore.service.BookService;
import com.example.bookstore.service.GenreService;
import com.example.bookstore.service.filter.BookFilter;
import com.example.bookstore.service.specification.BookSpecs;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Slf4j
public class BookAdminServiceImpl implements BookAdminService {

    private final BookService bookService;
    private final AuthorService authorService;
    private final GenreService genreService;
    private final BookEventPublisher bookEventPublisher;
    private final MeterRegistry meterRegistry;
    private Counter createCounter;
    private Counter updateCounter;
    private Counter deleteCounter;
    public BookAdminServiceImpl(BookService bookService,
                                AuthorService authorService,
                                GenreService genreService,
                                BookEventPublisher bookEventPublisher,
                                MeterRegistry meterRegistry) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.genreService = genreService;
        this.bookEventPublisher = bookEventPublisher;
        this.meterRegistry = meterRegistry;

        this.createCounter = Counter.builder("book.create.count")
                .description("Number of books created")
                .register(meterRegistry);

        this.updateCounter = Counter.builder("book.update.count")
                .description("Number of books updated")
                .register(meterRegistry);

        this.deleteCounter = Counter.builder("book.delete.count")
                .description("Number of books deleted")
                .register(meterRegistry);
    }
    @Override
    public Page<BookResponse> list(BookFilter filter, Pageable pageable) {
        Specification<Book> specification = BookSpecs.matches(
                filter.getQuery(),
                filter.getTitle(),
                filter.getAuthor(),
                filter.getGenre(),
                filter.getMinPrice(),
                filter.getMaxPrice()
        );
        return bookService.findAll(pageable, specification).map(BookMapper::toDto);
    }

    @Transactional
    @Timed(value = "books.create.timer", description = "Time to handle create-book command")
    public BookResponse create(BookRequest request) {
        Author author = authorService.findById(request.getAuthorId());
        Genre genre = genreService.findById(request.getGenreId());

        Book saved;
        try {
            Book entity = BookMapper.toEntity(request, author, genre);
            saved = bookService.saveAndFlush(entity);
        } catch (Exception e) {
            log.error("Failed to create book: {}", request, e);
            throw new BookStoreException("Failed to create book: " + request, e);
        }

        BookEvent event = BookEventMapper.toBookEvent(BookEventType.CREATE.getCode(), saved);
        publishBookEventAfterCommit(event, saved.getId());
        createCounter.increment();
        return BookMapper.toDto(saved);
    }

    @Transactional
    @Timed(value = "books.update.timer", description = "Time to handle update-book command")
    public BookResponse update(Long id, BookRequest request) {
        Author author = authorService.findById(request.getAuthorId());
        Genre genre = genreService.findById(request.getGenreId());
        Book existing = bookService.findById(id);

        Book updated;
        try {
            BookMapper.updateEntity(existing, request, author, genre);
            updated = bookService.saveAndFlush(existing);
        } catch (Exception e) {
            log.error("Failed to update book: {}", request, e);
            throw new BookStoreException("Failed to update book: " + request, e);
        }

        BookEvent event = BookEventMapper.toBookEvent(BookEventType.UPDATE.getCode(), updated);
        publishBookEventAfterCommit(event, updated.getId());
        updateCounter.increment();
        return BookMapper.toDto(updated);
    }

    @Override
    public BookResponse get(Long id) {
        return BookMapper.toDto(bookService.findById(id));
    }

    @Transactional
    @Timed(value = "books.delete.timer", description = "Time to handle delete-book command")
    public void delete(Long id) {
        Book existing = bookService.findById(id);

        try {
            bookService.delete(existing);
        } catch (Exception e) {
            log.error("Failed to delete bookId: {}", id, e);
            throw new BookStoreException("Failed to delete bookId: " + id, e);
        }

        BookEvent event = BookEventMapper.toBookEvent(BookEventType.DELETE.getCode(), id);
        publishBookEventAfterCommit(event, id);
        deleteCounter.increment();
    }

    private void publishBookEventAfterCommit(BookEvent event, Long bookId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            try {
                bookEventPublisher.publish(event);
                log.info("Published event (no-tx) for bookId={}", bookId);
            } catch (Exception ex) {
                log.error("Failed to publish event (no-tx) for bookId={}", bookId, ex);
            }
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    bookEventPublisher.publish(event);
                    log.info("Published event to Kafka for bookId={}", bookId);
                } catch (Exception ex) {
                    log.error("Failed to publish event to Kafka for bookId={}", bookId, ex);
                }
            }

            @Override
            public int getOrder() {
                return TransactionSynchronization.LOWEST_PRECEDENCE;
            }
        });
    }
}
