package com.example.bookstore.cache;

import com.example.bookstore.cache.impl.BookCacheFacade;
import com.example.bookstore.search.model.BookDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;

import static com.example.bookstore.cache.impl.BookCacheFacade.CACHE_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookCacheTest {

    @Mock
    CacheManager cacheManager;

    @Mock
    Cache cache;

    @Captor
    ArgumentCaptor<Long> keyCaptor;

    @Captor
    ArgumentCaptor<BookDocument> valueCaptor;

    private BookCache bookCache; // <— use the interface

    @BeforeEach
    void setUp() {
        when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
        this.bookCache = new BookCacheFacade(cacheManager); // instance under test
    }

    @Test
    void positive_putAllStoresUniqueDocs() {
        BookDocument d1 = mockDoc(1L);
        BookDocument d2 = mockDoc(2L);

        bookCache.putAll(List.of(d1, d2));

        verify(cache).put(1L, d1);
        verify(cache).put(2L, d2);
    }

    @Test
    void positive_putAllDeduplicates() {
        BookDocument d1 = mockDoc(1L);
        BookDocument d1dup = mockDoc(1L);

        bookCache.putAll(List.of(d1, d1dup));

        verify(cache, times(1)).put(eq(1L), any(BookDocument.class));
    }



    @Test
    void positive_evictRemovesEntry() {
        bookCache.evict(99L);
        verify(cache).evict(99L);
    }

    @Test
    void positive_clearRemovesAll() {
        bookCache.clear();
        verify(cache).clear();
    }


    @Test
    void negative_putAllNullCollection() {
        bookCache.putAll(null);
        verify(cache, never()).put(any(), any());
    }

    @Test
    void negative_putAllEmptyCollection() {
        bookCache.putAll(List.of());
        verify(cache, never()).put(any(), any());
    }

    @Test
    void negative_putAllSkipsNullIdDocs() {
        BookDocument dNull = mockDoc(null);
        bookCache.putAll(List.of(dNull));
        verify(cache, never()).put(any(), any());
    }

    @Test
    void negative_getAllByIdsNullList() {
        List<BookDocument> result = bookCache.getAllByIds(null);
        assertTrue(result.isEmpty());
        verify(cache, never()).get(any(), eq(BookDocument.class)); // disambiguates to (Object, Class)
    }

    @Test
    void negative_getAllByIdsEmptyList() {
        List<BookDocument> result = bookCache.getAllByIds(List.of());
        assertTrue(result.isEmpty());
        verify(cache, never()).get(any(), eq(BookDocument.class));
    }


    private static BookDocument mockDoc(Long id) {
        BookDocument doc = mock(BookDocument.class);
        when(doc.getId()).thenReturn(id);
        return doc;
    }
}
