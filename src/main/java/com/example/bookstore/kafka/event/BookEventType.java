package com.example.bookstore.kafka.event;


import com.fasterxml.jackson.annotation.JsonValue;

public enum BookEventType {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    SEARCH("search");

    private final String code;

    BookEventType(String code) {
        this.code = code;
    }


    @JsonValue
    public String getCode() {
        return code;
    }
}