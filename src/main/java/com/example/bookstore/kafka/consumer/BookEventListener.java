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

    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    @KafkaListener(
            topics = "${app.kafka.topics.book.events:book-events}",
            groupId = "${spring.kafka.consumer.group-id:bookstore-consumers}"
    )
    public void onEvent(BookEvent event) {
        if (event == null) {
            log.error("Received null BookEvent");
            return;
        }
        final String type = safeLower(event.getType());

        switch (type) {
            case "create" -> handleInsert(event);
            case "update" -> handleUpdate(event);
            case "delete" -> handleDelete(event);
            default -> {
                IllegalArgumentException ex =
                        new IllegalArgumentException("Unknown BookEvent type: " + event.getType());
                log.error("Unsupported event type for event id={}. Event={}", event.getId(), event, ex);
            }
        }
    }

    private void handleInsert(BookEvent event) {
        try {
            BookDocument doc = BookEventMapper.toDocument(event);

            searchRepository.save(doc);
            log.info("Insert book document id={} via event type={}", doc.getId(), event.getType());
        } catch (Exception e) {
            log.error("Failed to upsert document for event id={}. Event={}", event.getId(), event, e);
        }
    }

    private void handleUpdate(BookEvent event) {
        try {
            BookDocument doc = BookEventMapper.toDocument(event);
            searchRepository.deleteById(doc.getId());
            searchRepository.save(doc);

            log.info("Update book document id={} via event type={}", doc.getId(), event.getType());
        } catch (Exception e) {
            log.error("Failed to upsert document for event id={}. Event={}", event.getId(), event, e);
        }
    }


    private void handleDelete(BookEvent event) {
        try {
            if (event.getId() == null) {
                throw new IllegalArgumentException("Delete event must contain non-null id");
            }
            searchRepository.deleteById(event.getId());
            log.info("Deleted book document id={} via event type=delete", event.getId());
        } catch (Exception e) {
            log.error("Failed to delete document for event id={}. Event={}", event.getId(), event, e);
        }
    }



}