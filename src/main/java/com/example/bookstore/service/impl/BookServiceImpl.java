package com.example.bookstore.service.impl;

import com.example.bookstore.cache.BookCache;
import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.BookIndexProjection;
import com.example.bookstore.exception.BookStoreException;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.search.mapper.BookDocumentMapper;
import com.example.bookstore.search.model.BookDocument;
import com.example.bookstore.service.BookService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookCache bookCache;

    @Override
    @Transactional(readOnly = true)
    @Timed(
            value = "books.indexing.timer",
            description = "Time to execute a book search",
            extraTags = {"component", "booking-service"}
    )
    public Page<BookIndexProjection> findBooksForIndexing(Pageable pageable) {
        var page = bookRepository.findAllForIndexing(pageable);
        log.debug("Loaded {} projections (page {} of {}) for indexing",
                page.getNumberOfElements(), page.getNumber() + 1, page.getTotalPages());
        return page;
    }

    @Override
    @Timed(
            value = "books.findByIddb.timer",
            description = "Time to execute a book search",
            extraTags = {"component", "booking-service"}
    )
    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookStoreException("Book not found: " + id));
    }

    @Override
    public Page<Book> findAll(Pageable pageable, Specification<Book> spec) {
        return bookRepository.findAll(spec, pageable);
    }

    @Override
    public Book saveAndFlush(Book book) {
        return bookRepository.saveAndFlush(book);
    }

    @Override
    @Timed(
            value = "books.deletedb.timer",
            description = "Time to execute a book search",
            extraTags = {"component", "booking-service"}
    )
    public void delete(Book book) {
        bookRepository.delete(book);
    }

    public List<Book> findBooksByIds(List<Long> ids) {
        return (!CollectionUtils.isEmpty(ids)) ?
                bookRepository.findAllById(ids) : List.of();
    }

    @Override
    @Transactional(readOnly = true)
    @Timed(
            value = "books.getDocumentsByIds.timer",
            description = "Time to execute a book search",
            extraTags = {"component", "booking-service"}
    )
    public List<BookDocument> getDocumentsByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        List<BookDocument> cachedDocuments = bookCache.getAllByIds(ids);

        System.out.println("cachedDocuments=" + cachedDocuments.toString());
        Set<Long> cachedIds = cachedDocuments.stream()
                .filter(Objects::nonNull)
                .map(BookDocument::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<Long> missingIds = ids.stream()
                .filter(Objects::nonNull)
                .filter(id -> !cachedIds.contains(id))
                .toList();

        List<Book> missingBooks = findBooksByIds(missingIds);
        List<BookDocument> missingDocuments = missingBooks.stream()
                .map(BookDocumentMapper::toDocument)
                .filter(Objects::nonNull)
                .toList();

        if (!missingDocuments.isEmpty()) {
            bookCache.putAll(missingDocuments);
        }

        List<BookDocument> result = new ArrayList<>(cachedDocuments.size() + missingDocuments.size());
        result.addAll(cachedDocuments);
        result.addAll(missingDocuments);

        return result.stream()
                .filter(Objects::nonNull)
                .toList();
    }


}
