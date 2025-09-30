package com.example.bookstore.service;



import com.example.bookstore.domain.Author;

public interface AuthorService {
    Author findById(Long id);
}
