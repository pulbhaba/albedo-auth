package com.akbo.auth.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AbstractDto {
    private Long id;
    private LocalDateTime createdTime;
    private LocalDateTime lastUpdatedTime;
    private String createdBy;
    private String lastUpdatedBy;
}
