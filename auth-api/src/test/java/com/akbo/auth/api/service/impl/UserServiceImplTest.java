package com.akbo.auth.api.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.akbo.auth.dao.entity.User;
import com.akbo.auth.dao.repository.UserRepository;
import com.akbo.auth.dto.Role;
import com.akbo.auth.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @Mock private ModelMapper modelMapper;

    @InjectMocks private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");

        userDto = new UserDto();
        userDto.setUsername("testuser");
        userDto.setPassword("password");
        userDto.setRoles(Set.of(Role.ROLE_USER));
    }

    @Test
    void createUser_newUserInfo() {
        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto created = userService.createUser(userDto);

        assertNotNull(created);
        assertEquals(userDto.getUsername(), created.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_existingUser_byId() {
        userDto.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_userDetails() {
        UserDetails userDetails = mock(UserDetails.class);
        when(modelMapper.map(userDetails, UserDto.class)).thenReturn(userDto);
        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.createUser(userDetails);

        verify(userRepository).save(any());
    }

    @Test
    void updateUser() {
        UserDetails userDetails = user;
        userService.updateUser(userDetails);
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_exists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        userService.deleteUser("testuser");
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_notExists() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.deleteUser("unknown"));
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void userExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        assertTrue(userService.userExists("testuser"));
    }

    @Test
    void loadUserByUsername_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        assertEquals(user, userService.loadUserByUsername("testuser"));
    }

    @Test
    void loadUserByUsername_notFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(
                UsernameNotFoundException.class, () -> userService.loadUserByUsername("unknown"));
    }

    @Test
    void getUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        UserDto result = userService.getUser("testuser");
        assertNotNull(result);
        assertEquals(userDto.getUsername(), result.getUsername());
    }
}
