package com.example.bookstore.kafka.consumer;

import com.example.bookstore.kafka.event.BookEvent;
import com.example.bookstore.kafka.mapper.BookEventMapper;
import com.example.bookstore.search.model.BookDocument;
import com.example.bookstore.search.repository.BookSearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BookEventListener {
    private final BookSearchRepository searchRepository;

    public BookEventListener(BookSearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    @KafkaListener(topics = "${app.kafka.topics.book.events:book-events}",
            groupId = "${spring.kafka.consumer.group-id:bookstore-consumers}")
    public void onEvent(BookEvent event) {

        System.out.println("onEvent");
        switch (event.getType()) {
            case "create", "update" -> {
                try {
                    System.out.println("event=" + event);
                    BookDocument bookDocument = BookEventMapper.toDocument(event);
                    searchRepository.save(bookDocument);
                } catch (Exception e) {
                    log.error("error while processing data from kafka topic event:{}", event, e);
                }
            }
            case "delete" -> {
                try {
                    searchRepository.deleteById(event.getId());
                } catch (Exception e) {
                    log.error("error while processing data from kafka delete topic event:{}", event, e);
                }

            }
        }
    }
}