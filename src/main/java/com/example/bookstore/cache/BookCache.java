package com.example.bookstore.cache;


import com.example.bookstore.search.model.BookDocument;

import java.util.Collection;
import java.util.List;

public interface BookCache {
    void putAll(Collection<BookDocument> documents);

    List<BookDocument> getAllByIds(List<Long> ids);

    void evict(Long id);

    void clear();
}