package com.akbo.auth.dto;

import lombok.Data;

@Data
public class PasswordChangeDto {
    private String requestKey;
    private String newPassword;
}
