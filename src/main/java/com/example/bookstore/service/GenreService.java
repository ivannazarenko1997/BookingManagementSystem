package com.example.bookstore.service;


import com.example.bookstore.domain.Genre;

public interface GenreService {

    Genre findById(Long id);
}