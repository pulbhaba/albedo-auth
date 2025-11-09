package com.akbo.auth.dao.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

@Setter
@Entity
@Audited(withModifiedFlag = true)
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username"})})
@EqualsAndHashCode(callSuper = true)
public class User extends AbstractEntity implements UserDetails {

    @Getter
    private String username;
    @Getter
    private String password;
    @Getter
    private String firstName;
    @Getter
    private String lastName;
    @Getter
    private String emailAddress;
    private Boolean enabled;
    private Boolean accountNonExpired = true;
    private Boolean credentialsNonExpired = true;
    private Boolean accountNonLocked = true;

    @Getter
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role"))
    private Set<UserRole> authorities;

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
