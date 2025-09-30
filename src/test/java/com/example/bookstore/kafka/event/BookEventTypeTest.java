package com.example.bookstore.kafka.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookEventTypeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    static Stream<Object[]> cases() {
        return Stream.of(
                new Object[]{BookEventType.CREATE, "create"},
                new Object[]{BookEventType.UPDATE, "update"},
                new Object[]{BookEventType.DELETE, "delete"},
                new Object[]{BookEventType.SEARCH, "search"}
        );
    }

    @ParameterizedTest
    @MethodSource("cases")
    @DisplayName("getCode returns expected value")
    void getCodeReturnsExpected(BookEventType type, String expected) {
        assertEquals(expected, type.getCode());
    }

    @ParameterizedTest
    @MethodSource("cases")
    @DisplayName("Jackson serializes enum to code via @JsonValue")
    void jacksonSerializesToCode(BookEventType type, String expected) throws Exception {
        assertEquals('"' + expected + '"', objectMapper.writeValueAsString(type));
    }

    @Test
    @DisplayName("All codes are unique")
    void codesAreUnique() {
        Set<String> codes = new HashSet<>();
        Arrays.stream(BookEventType.values()).forEach(t -> codes.add(t.getCode()));
        assertEquals(BookEventType.values().length, codes.size());
    }
}
