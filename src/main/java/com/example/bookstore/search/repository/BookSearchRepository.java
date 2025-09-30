package com.example.bookstore.search.repository;

import com.example.bookstore.search.model.BookDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BookSearchRepository extends ElasticsearchRepository<BookDocument, Long> {
    Page<BookDocument> findAll(Pageable pageable);
}
