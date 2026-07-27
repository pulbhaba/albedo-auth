package com.akbo.auth.dto;

import lombok.Data;

@Data
public class LogoutRequestDto {
    private String refreshToken;
}
