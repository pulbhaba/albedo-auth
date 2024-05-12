package com.akbo.auth.dao.entity;

import java.util.List;
import org.hibernate.envers.Audited;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Data
@Entity
@Audited(withModifiedFlag = true)
@Table(name = "users")
@EqualsAndHashCode(callSuper = true)
public class User extends AbstractEntity implements UserDetails {

    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private Boolean enabled;

    @OneToMany(targetEntity = Role.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "userId")
    private List<Role> authorities;

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.getEnabled();
    }
}
