package com.akbo.auth.dao.entity;

import com.akbo.auth.dto.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

/**
 *
 */
@Data
@Table(name = "roles")
@Entity
@Audited(withModifiedFlag = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserRole implements GrantedAuthority {

    @Id
    @Enumerated(EnumType.STRING)
    private Role role;

    @Override
    public String getAuthority() {
        return role.getRoleName();
    }
}


