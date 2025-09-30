package com.example.bookstore.search.initialization;



import com.example.bookstore.domain.BookIndexProjection;
import com.example.bookstore.search.mapper.BookDocumentMapper;
import com.example.bookstore.search.model.BookDocument;
import com.example.bookstore.search.repository.BookSearchRepository;
import com.example.bookstore.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SearchReindexer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(SearchReindexer.class);

    private final BookService bookService;
    private final BookSearchRepository searchRepository;

    @Value("${app.search.reindex-on-start:false}")
    private boolean reindexOnStart;

    @Value("${app.search.reindex.batch-size:1000}")
    private int batchSize;

    @Value("${app.search.reindex.fail-on-error:false}")
    private boolean failOnError;

    public SearchReindexer(BookService bookService, BookSearchRepository searchRepository) {
        this.bookService = bookService;
        this.searchRepository = searchRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting Books → Elasticsearch reindex (batchSize={})", batchSize);
        if (!reindexOnStart) {
            log.info("Search reindex on start is disabled. Skipping.");
            return;
        }

        loadToElasicSearch();
    }

    private void loadToElasicSearch() {
        long start = System.currentTimeMillis();
        int page = 0;
        long totalIndexed = 0;
        try {
            while (true) {
                Page<BookIndexProjection> slice = bookService.findBooksForIndexing(PageRequest.of(page, batchSize));
                if (!slice.isEmpty()) {
                    List<BookDocument> docs = slice.getContent().stream().map(BookDocumentMapper::toDocument).toList();
                    for (int i = 0; i < docs.size(); i++) {
                        System.out.println("Doc " + i + ": " + docs.get(i).getId());
                    }
                    searchRepository.saveAll(docs);

                    Iterable<BookDocument> list = searchRepository.findAll();
                    for (BookDocument book : list) {
                        System.out.println("book =" + book.toString()); // or access fields like book.getTitle()
                    }
                    totalIndexed += docs.size();

                    log.info("Indexed batch page={} size={} (total={})", page, docs.size(), totalIndexed);

                }
                if (!slice.hasNext()) {
                    break;
                }
                page++;
            }
            long took = System.currentTimeMillis() - start;
            log.info("ElasticSearch Reindex complete. {} docs in {} ms", totalIndexed, took);
        } catch (Exception e) {
            log.error("ElasticSearch Reindex failed after {} docs", totalIndexed, e);
            if (failOnError) {
                throw e instanceof RuntimeException re ? re : new RuntimeException(e);
            }
        }
    }

}
