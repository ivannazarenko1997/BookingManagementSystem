package com.example.bookstore.service;


import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.BookIndexProjection;
import com.example.bookstore.search.model.BookDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface BookService {

    Page<BookIndexProjection> findBooksForIndexing(Pageable pageable);

    Book findById(Long id);

    Page<Book> findAll(Pageable pageable, Specification<Book> spec);

    Book saveAndFlush(Book book);

    void delete(Book book);

    List<BookDocument> getDocumentsByIds(List<Long> ids);
}