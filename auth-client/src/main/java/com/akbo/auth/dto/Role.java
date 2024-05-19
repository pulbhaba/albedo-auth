package com.akbo.auth.dto;

import lombok.Getter;

@Getter
public enum Role {
    ROLE_ADMIN("ROLE_ADMIN", "Managing user registration/removal"),
    ROLE_POWER_USER("ROLE_POWER_USER", "Additional access layer"),
    ROLE_USER("ROLE_USER", "Accessing own information");

    private String roleName;
    private String roleDescription;

    Role(final String roleName, final String roleDescription) {
        this.roleName = roleName;
        this.roleDescription = roleDescription;
    }
}
