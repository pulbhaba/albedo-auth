package com.akbo.auth.api.controller;

import com.akbo.auth.api.service.PasswordService;
import com.akbo.auth.api.service.UserService;
import com.akbo.auth.aspect.GlobalExceptionHandler;
import com.akbo.auth.dto.PasswordChangeDto;
import com.akbo.auth.dto.Role;
import com.akbo.auth.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PublicControllerTest {

    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    PasswordService passwordService;

    @Mock
    UserService userService;

    @InjectMocks
    PublicController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void changePassword_returnsUserDto() throws Exception {
        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setRequestKey("req-123");
        dto.setNewPassword("new");

        UserDto resp = new UserDto();
        resp.setId(1L);
        resp.setUsername("alice");
        resp.setFirstName("Alice");
        resp.setLastName("Liddell");
        resp.setEmailAddress("alice@example.com");
        resp.setEnabled(true);
        resp.setRoles(Set.of(Role.ROLE_USER));

        when(passwordService.changePassword(any(PasswordChangeDto.class))).thenReturn(resp);

        mockMvc.perform(post("/public/change-password/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.firstName").value("Alice"));

        ArgumentCaptor<PasswordChangeDto> captor = ArgumentCaptor.forClass(PasswordChangeDto.class);
        verify(passwordService).changePassword(captor.capture());
        assertThat(captor.getValue().getRequestKey()).isEqualTo("req-123");
    }

    @Test
    void requestPasswordReset_invokesService_andReturnsOk() throws Exception {
        mockMvc.perform(get("/public/user/{username}/reset-password", "bob"))
                .andExpect(status().isOk());
        verify(passwordService, times(1)).RequestPasswordChange(eq("bob"));
    }

    @Test
    void registerUser_returnsCreatedUser() throws Exception {
        UserDto req = new UserDto();
        req.setUsername("charlie");
        req.setPassword("secret");

        UserDto saved = new UserDto();
        saved.setId(42L);
        saved.setUsername("charlie");
        saved.setEnabled(true);

        when(userService.createUser(any(UserDto.class))).thenReturn(saved);

        mockMvc.perform(post("/public/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.username").value("charlie"));

        verify(userService).createUser(any(UserDto.class));
    }
}