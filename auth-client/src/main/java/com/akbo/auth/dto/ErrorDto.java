package com.akbo.auth.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ErrorDto {
    private String message;
    private Integer statusCode;
    private Map<String, Object> data = new HashMap<>();

    public void putItem(final String key, final Object value) {
        data.put(key, value);
    }
}
