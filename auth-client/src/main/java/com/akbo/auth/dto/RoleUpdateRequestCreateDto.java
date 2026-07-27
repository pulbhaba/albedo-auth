package com.akbo.auth.dto;

import lombok.Data;

@Data
public class RoleUpdateRequestCreateDto {
    private Role requestedRole;
    private String evidence;
}
