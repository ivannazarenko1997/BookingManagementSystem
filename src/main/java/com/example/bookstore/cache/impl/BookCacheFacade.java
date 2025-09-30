package com.example.bookstore.cache.impl;

import com.example.bookstore.cache.BookCache;
import com.example.bookstore.search.model.BookDocument;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class BookCacheFacade implements BookCache {

    public static final String CACHE_NAME = "booksById";

    private final Cache booksByIdCache;

    public BookCacheFacade(@Qualifier("redisCacheManager") CacheManager cacheManager) {
        this.booksByIdCache = Objects.requireNonNull(
                cacheManager.getCache(CACHE_NAME),
                () -> "Cache '" + CACHE_NAME + "' not found"
        );
    }

    public void putAll(Collection<BookDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        final Set<Long> uniqueIds = new LinkedHashSet<>();
        for (BookDocument doc : documents) {
            if (doc == null) {
                continue;
            }
            final Long id = doc.getId();
            if (id == null) {
                continue;
            }
            if (uniqueIds.add(id)) {
                booksByIdCache.put(id, doc);
            }
        }
    }

    public List<BookDocument> getAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        final List<BookDocument> result = new ArrayList<>(ids.size());
        final Set<Long> seen = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id == null || !seen.add(id)) {
                continue;
            }
            BookDocument doc = booksByIdCache.get(id, BookDocument.class);
            if (doc != null) {
                result.add(doc);
            }
        }
        return result;
    }

    public void evict(Long id) {
        if (id != null) {
            booksByIdCache.evict(id);
        }
    }

    public void clear() {
        booksByIdCache.clear();
    }
}
