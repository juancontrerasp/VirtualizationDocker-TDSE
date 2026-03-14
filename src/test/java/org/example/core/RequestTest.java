package org.example.core;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {

    @Test
    void getValues_existingKey_returnsValue() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "Alice");
        Request request = new Request(params);

        assertEquals("Alice", request.getValues("name"));
    }

    @Test
    void getValues_missingKey_returnsNull() {
        Request request = new Request(new HashMap<>());

        assertNull(request.getValues("nonexistent"));
    }

    @Test
    void getValues_multipleParams_returnsCorrectValue() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "Bob");
        params.put("age", "30");
        Request request = new Request(params);

        assertEquals("Bob", request.getValues("name"));
        assertEquals("30", request.getValues("age"));
    }

    @Test
    void getValues_emptyStringValue_returnsEmptyString() {
        Map<String, String> params = new HashMap<>();
        params.put("flag", "");
        Request request = new Request(params);

        assertEquals("", request.getValues("flag"));
    }
}
