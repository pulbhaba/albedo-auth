package com.akbo.auth.dto;

import lombok.Getter;

@Getter
public enum Role {
    ROLE_ADMIN("ROLE_ADMIN");

    private String roleName;

    Role(final String roleName) {
        this.roleName = roleName;
    }
}
