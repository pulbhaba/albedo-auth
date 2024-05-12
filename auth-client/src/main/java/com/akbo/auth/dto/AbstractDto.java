package com.akbo.auth.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class AbstractDto {
    private UUID id;
    private Instant createdTime;
    private Instant updatedInstant;
    private String createdBy;
    private String UpdatedBy;
}
