package com.example.bookstore.kafka.producer;



import com.example.bookstore.kafka.event.BookEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class BookEventPublisherTest {

    private KafkaTemplate<String, BookEvent> kafkaTemplate;
    private BookEventPublisher publisher;

    @BeforeEach
    void setUp() throws Exception {
        kafkaTemplate = mock(KafkaTemplate.class);
        publisher = new BookEventPublisher(kafkaTemplate);

        var topicField = BookEventPublisher.class.getDeclaredField("topic");
        topicField.setAccessible(true);
        topicField.set(publisher, "book-events");
    }

    @Test
    void shouldSendEventToKafkaTopic() {
        BookEvent event = new BookEvent();
        event.setId(1L);
        event.setTitle("Effective Java");

        publisher.publish(event);

        verify(kafkaTemplate).send("book-events", "1", event);
    }

    @Test
    void shouldUseEventIdAsKey() {
        BookEvent event = new BookEvent();
        event.setId(42L);
        event.setTitle("Clean Code");

        publisher.publish(event);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("book-events"), keyCaptor.capture(), eq(event));
        assertThat(keyCaptor.getValue()).isEqualTo("42");
    }

    @Test
    void shouldPublishMultipleEvents() {
        BookEvent event1 = new BookEvent();
        event1.setId(100L);
        event1.setTitle("Book One");

        BookEvent event2 = new BookEvent();
        event2.setId(101L);
        event2.setTitle("Book Two");

        publisher.publish(event1);
        publisher.publish(event2);

        verify(kafkaTemplate).send("book-events", "100", event1);
        verify(kafkaTemplate).send("book-events", "101", event2);
    }

    @Test
    void shouldPublishBookEventToKafkaTopic() {
        // Given
        BookEvent event = new BookEvent();
        event.setId(123L);
        event.setTitle("Kafka in Action");

        // When
        publisher.publish(event);

        // Then
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BookEvent> eventCaptor = ArgumentCaptor.forClass(BookEvent.class);

        verify(kafkaTemplate, times(1)).send(eq("book-events"), keyCaptor.capture(), eventCaptor.capture());

        assertThat(keyCaptor.getValue()).isEqualTo("123");
        assertThat(eventCaptor.getValue().getTitle()).isEqualTo("Kafka in Action");
    }

    @Test
    void shouldNotSendNullEvent() {
        assertThrows(NullPointerException.class, () -> publisher.publish(null));
    }

    @Test
    void shouldHandleKafkaSendFailure() {
        BookEvent event = new BookEvent();
        event.setId(5L);
        event.setTitle("Failure Test");

        doThrow(new RuntimeException("Kafka error")).when(kafkaTemplate).send(any(), any(), any());

        assertThrows(RuntimeException.class, () -> publisher.publish(event));
    }


}
