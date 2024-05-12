package com.akbo.auth.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AbstractDto {
    private Long id;
    private LocalDateTime createdTime;
    private LocalDateTime lastUpdatedTime;
    private String createdBy;
    private String lastUpdatedBy;
}
