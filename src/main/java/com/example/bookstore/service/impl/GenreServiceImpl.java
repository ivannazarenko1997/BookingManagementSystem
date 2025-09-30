package com.example.bookstore.service.impl;


import com.example.bookstore.domain.Genre;
import com.example.bookstore.exception.BookStoreException;
import com.example.bookstore.repository.GenreRepository;
import com.example.bookstore.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {
    private final GenreRepository genreRepository;

    @Override
    public Genre findById(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new BookStoreException("Genre not found: " + id));
    }
}
