package com.example.bookstore.service.filter;


import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BookFilterTest {

    @Test
    void shouldCreateFilterWithAllFields() {
        BookFilter filter = new BookFilter(
                "search term",
                "Effective Java",
                "Joshua Bloch",
                "Programming",
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(50)
        );

        assertThat(filter.getQuery()).isEqualTo("search term");
        assertThat(filter.getTitle()).isEqualTo("Effective Java");
        assertThat(filter.getAuthor()).isEqualTo("Joshua Bloch");
        assertThat(filter.getGenre()).isEqualTo("Programming");
        assertThat(filter.getMinPrice()).isEqualTo(BigDecimal.valueOf(10));
        assertThat(filter.getMaxPrice()).isEqualTo(BigDecimal.valueOf(50));
    }

    @Test
    void shouldHandleNullValuesGracefully() {
        BookFilter filter = new BookFilter(null, null, null, null, null, null);

        assertThat(filter.getQuery()).isNull();
        assertThat(filter.getTitle()).isNull();
        assertThat(filter.getAuthor()).isNull();
        assertThat(filter.getGenre()).isNull();
        assertThat(filter.getMinPrice()).isNull();
        assertThat(filter.getMaxPrice()).isNull();
    }

    @Test
    void shouldAllowEmptyStrings() {
        BookFilter filter = new BookFilter("", "", "", "", null, null);

        assertThat(filter.getQuery()).isEmpty();
        assertThat(filter.getTitle()).isEmpty();
        assertThat(filter.getAuthor()).isEmpty();
        assertThat(filter.getGenre()).isEmpty();
    }

    @Test
    void shouldSupportZeroAndNegativePrices() {
        BookFilter filter = new BookFilter(null, null, null, null, BigDecimal.ZERO, BigDecimal.valueOf(-1));

        assertThat(filter.getMinPrice()).isEqualTo(BigDecimal.ZERO);
        assertThat(filter.getMaxPrice()).isEqualTo(BigDecimal.valueOf(-1));
    }
}
