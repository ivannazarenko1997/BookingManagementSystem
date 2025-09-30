package com.example.bookstore.repository;

import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.BookIndexProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    @Query("""
            select
                b.id as id,
                b.title as title,
                a.name as authorName,
                g.name as genreName,
                b.price as price 
            from Book b
            join b.author a
            join b.genre g
            """)
    Page<BookIndexProjection> findAllForIndexing(Pageable pageable);
}