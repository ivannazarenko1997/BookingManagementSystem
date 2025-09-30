package com.example.bookstore.service.impl;


import com.example.bookstore.domain.Author;
import com.example.bookstore.exception.BookStoreException;
import com.example.bookstore.repository.AuthorRepository;
import com.example.bookstore.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    @Override
    public Author findById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new BookStoreException("Author not found: " + id));
    }
}
