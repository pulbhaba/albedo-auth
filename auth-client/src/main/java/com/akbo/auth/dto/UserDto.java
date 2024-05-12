package com.akbo.auth.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDto extends AbstractDto {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private Boolean enabled;
}