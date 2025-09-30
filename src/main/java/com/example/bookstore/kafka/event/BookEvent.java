package com.example.bookstore.kafka.event;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BookEvent {

    private Long id;
    private String type;
    private String title;
    private String authorName;
    private String genreName;
    private BigDecimal price;
    private String caption;

}