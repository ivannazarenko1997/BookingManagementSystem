package com.example.bookstore.kafka.mapper;


import com.example.bookstore.domain.Book;
import com.example.bookstore.kafka.event.BookEvent;
import com.example.bookstore.search.model.BookDocument;

public final class BookEventMapper {
    private BookEventMapper() {
    }
    public static BookEvent toBookEvent(String type, Book entity) {
        return BookEvent.builder()
                .id(entity.getId())
                .type(type)
                .title(entity.getTitle())
                .authorName(entity.getAuthor().getName())
                .genreName(entity.getGenre().getName())
                .price(entity.getPrice())
                .caption(entity.getCaption())
                .build();
    }

    public static BookEvent toBookEvent(String type, Long id) {
        return BookEvent.builder()
                .id(id)
                .type(type)
                .build();
    }

    public static BookDocument toDocument(BookEvent event) {
        if (event == null) {
            return null;
        }

        return BookDocument.builder()
                .id(event.getId())
                .title(event.getTitle())
                .authorName(event.getAuthorName())
                .genreName(event.getGenreName())
                .price(event.getPrice())
                .build();
    }


}