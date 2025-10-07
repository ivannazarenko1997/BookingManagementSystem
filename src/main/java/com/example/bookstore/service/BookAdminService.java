package com.example.bookstore.service;

import com.example.bookstore.dto.BookRequest;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.service.filter.BookFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookAdminService {

    BookResponse create(BookRequest req);

    BookResponse update(Long id, BookRequest req);

    BookResponse get(Long id);

    void delete(Long id);
}
