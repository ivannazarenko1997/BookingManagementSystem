package com.example.bookstore.domain;

import java.math.BigDecimal;


public interface BookIndexProjection {
    Long getId();

    String getTitle();

    String getAuthorName();

    String getGenreName();

    BigDecimal getPrice();
}