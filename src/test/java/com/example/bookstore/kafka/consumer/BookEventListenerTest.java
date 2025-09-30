package com.example.bookstore.kafka.consumer;


import com.example.bookstore.kafka.event.BookEvent;
import com.example.bookstore.kafka.mapper.BookEventMapper;
import com.example.bookstore.search.model.BookDocument;
import com.example.bookstore.search.repository.BookSearchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookEventListenerTest {

    @org.mockito.Mock
    private BookSearchRepository searchRepository;

    @Test
    @DisplayName("create -> saves mapped document")
    void shouldSaveDocumentOnCreateEvent() {
        BookEventListener listener = new BookEventListener(searchRepository);
        BookEvent event = mock(BookEvent.class);
        when(event.getType()).thenReturn("create");

        BookDocument doc = new BookDocument();
        doc.setId(1L);

        try (MockedStatic<BookEventMapper> mocked = mockStatic(BookEventMapper.class)) {
            mocked.when(() -> BookEventMapper.toDocument(event)).thenReturn(doc);
            listener.onEvent(event);
            verify(searchRepository).save(doc);
            verifyNoMoreInteractions(searchRepository);
        }
    }

    @Test
    @DisplayName("update -> saves mapped document")
    void shouldSaveDocumentOnUpdateEvent() {
        BookEventListener listener = new BookEventListener(searchRepository);
        BookEvent event = mock(BookEvent.class);
        when(event.getType()).thenReturn("update");

        BookDocument doc = new BookDocument();
        doc.setId(2L);

        try (MockedStatic<BookEventMapper> mocked = mockStatic(BookEventMapper.class)) {
            mocked.when(() -> BookEventMapper.toDocument(event)).thenReturn(doc);
            listener.onEvent(event);
            verify(searchRepository).save(doc);
            verifyNoMoreInteractions(searchRepository);
        }
    }

    @Test
    @DisplayName("delete -> deletes by id")
    void shouldDeleteByIdOnDeleteEvent() {
        BookEventListener listener = new BookEventListener(searchRepository);
        BookEvent event = mock(BookEvent.class);
        when(event.getType()).thenReturn("delete");
        when(event.getId()).thenReturn(3L);

        listener.onEvent(event);

        verify(searchRepository).deleteById(3L);
        verifyNoMoreInteractions(searchRepository);
    }

    @Test
    @DisplayName("create -> swallows repository save exception")
    void shouldSwallowExceptionWhenSaveFails() {
        BookEventListener listener = new BookEventListener(searchRepository);
        BookEvent event = mock(BookEvent.class);
        when(event.getType()).thenReturn("create");

        BookDocument doc = new BookDocument();
        doc.setId(4L);

        try (MockedStatic<BookEventMapper> mocked = mockStatic(BookEventMapper.class)) {
            mocked.when(() -> BookEventMapper.toDocument(event)).thenReturn(doc);
            doThrow(new RuntimeException("boom")).when(searchRepository).save(doc);
            listener.onEvent(event);
            verify(searchRepository).save(doc);
            verifyNoMoreInteractions(searchRepository);
        }
    }

    @Test
    @DisplayName("create -> swallows mapper exception and does not call save")
    void shouldSwallowExceptionWhenMapperFails() {
        BookEventListener listener = new BookEventListener(searchRepository);
        BookEvent event = mock(BookEvent.class);
        when(event.getType()).thenReturn("create");

        try (MockedStatic<BookEventMapper> mocked = mockStatic(BookEventMapper.class)) {
            mocked.when(() -> BookEventMapper.toDocument(event)).thenThrow(new RuntimeException("mapping failed"));
            listener.onEvent(event);
            verifyNoInteractions(searchRepository);
        }
    }

    @Test
    @DisplayName("delete -> swallows repository delete exception")
    void shouldSwallowExceptionWhenDeleteFails() {
        BookEventListener listener = new BookEventListener(searchRepository);
        BookEvent event = mock(BookEvent.class);
        when(event.getType()).thenReturn("delete");
        when(event.getId()).thenReturn(6L);

        doThrow(new RuntimeException("boom")).when(searchRepository).deleteById(6L);

        listener.onEvent(event);

        verify(searchRepository).deleteById(6L);
        verifyNoMoreInteractions(searchRepository);
    }
}
