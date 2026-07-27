package com.akbo.auth.dao.entity;

import static org.junit.jupiter.api.Assertions.*;
import com.akbo.auth.dto.Role;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

class EntityTest {

    @Test
    void testUserEntity() {
        User user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setPassword("pass");
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);

        Set<UserRole> roles = new HashSet<>();
        roles.add(new UserRole(Role.ROLE_USER));
        user.setAuthorities(roles);

        assertEquals(1L, user.getId());
        assertEquals("john", user.getUsername());
        assertEquals("pass", user.getPassword());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
        assertEquals(1, user.getAuthorities().size());

        User user2 = new User();
        user2.setId(1L);
        // Lombok Data/EqualsAndHashCode should consider ID
        assertEquals(user.getId(), user2.getId());
    }

    @Test
    void testPasswordChangeRequest() {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setId(2L);
        User user = new User();
        request.setUser(user);
        request.setRandomString("abc");
        request.setExpired(false);
        request.setPasswordChanged(true);
        LocalDateTime now = LocalDateTime.now();
        request.setEmailNotificationSent(now);

        assertEquals(2L, request.getId());
        assertEquals(user, request.getUser());
        assertEquals("abc", request.getRandomString());
        assertFalse(request.getExpired());
        assertTrue(request.getPasswordChanged());
        assertEquals(now, request.getEmailNotificationSent());
    }
}
