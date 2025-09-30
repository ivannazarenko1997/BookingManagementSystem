package com.example.bookstore.kafka.producer;

import com.example.bookstore.kafka.event.BookEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class BookEventPublisher {
    private final KafkaTemplate<String, BookEvent> template;

    @Value("${app.kafka.topics.book.events:book-events}")
    private String topic;


    public BookEventPublisher(KafkaTemplate<String, BookEvent> template) {
        this.template = template;
    }

    public void publish(BookEvent event) {
        System.out.println("topic=" + topic + " publish event=" + event.toString());
        template.send(topic, String.valueOf(event.getId()), event);
    }
}