package com.akbo.auth.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleUpdateRequestDto extends AbstractDto {
    private String username;
    private String firstName;
    private String lastName;
    private Role requestedRole;
    private String evidence;
    private RoleUpdateRequestStatus status;
    private String resolvedBy;
    private LocalDateTime resolvedAt;
    private String resolutionReason;
}
