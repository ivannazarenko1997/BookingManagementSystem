package com.example.bookstore.web;

import lombok.Data;

import java.time.Instant;

@Data
public class ApiError {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public static ApiError of(int status, String error, String message, String path) {
        ApiError e = new ApiError();
        e.timestamp = Instant.now();
        e.status = status;
        e.error = error;
        e.message = message;
        e.path = path;
        return e;
    }

}