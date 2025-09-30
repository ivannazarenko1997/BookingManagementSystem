package com.example.bookstore.kafka.producer;

import com.example.bookstore.kafka.event.BookEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BookEventPublisher {
    private final KafkaTemplate<String, BookEvent> template;

    @Value("${app.kafka.topics.book.events:book-events}")
    private String topic;


    public BookEventPublisher(KafkaTemplate<String, BookEvent> template) {
        this.template = template;
    }

    public void publish(BookEvent event) {
        log.info("Publishing event to topic={} event={}", topic, event);
        template.send(topic, String.valueOf(event.getId()), event);
    }
}