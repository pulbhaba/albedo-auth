package com.akbo.auth.dto;

import java.util.*;
import lombok.Data;

@Data
public class ErrorDto {
    private String message;
    private Integer statusCode;
    private Map<String, Object> data = new HashMap<>();

    public void putItem(final String key, final Object value){
        data.put(key, value);
    }
}
