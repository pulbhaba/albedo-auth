package com.akbo.auth.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.akbo.auth.api.service.RoleUpdateRequestService;
import com.akbo.auth.api.service.TokenLogoutService;
import com.akbo.auth.api.service.UserService;
import com.akbo.auth.aspect.GlobalExceptionHandler;
import com.akbo.auth.dto.Role;
import com.akbo.auth.dto.RoleUpdateRequestCreateDto;
import com.akbo.auth.dto.RoleUpdateRequestDto;
import com.akbo.auth.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    MockMvc mockMvc;

    @Mock UserService userService;
    @Mock TokenLogoutService tokenLogoutService;
    @Mock RoleUpdateRequestService roleUpdateRequestService;

    @InjectMocks UserController controller;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.standaloneSetup(controller)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
    }

    @Test
    void getUser_returnsUserDto() throws Exception {
        UserDto dto = new UserDto();
        dto.setId(7L);
        dto.setUsername("dora");
        when(userService.getUser(eq("dora"))).thenReturn(dto);

        mockMvc.perform(get("/user/{username}", "dora").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.username").value("dora"));
    }

    @Test
    void requestRoleUpdate_usesAuthenticatedSubject() {
        Jwt jwt = jwt("dora");
        RoleUpdateRequestCreateDto createDto = new RoleUpdateRequestCreateDto();
        createDto.setRequestedRole(Role.ROLE_EDITOR);
        RoleUpdateRequestDto responseDto = new RoleUpdateRequestDto();
        responseDto.setUsername("dora");
        responseDto.setRequestedRole(Role.ROLE_EDITOR);
        when(roleUpdateRequestService.createRequest("dora", createDto)).thenReturn(responseDto);

        ResponseEntity<RoleUpdateRequestDto> response =
                controller.requestRoleUpdate(jwt, createDto);

        assertNotNull(response.getBody());
        assertEquals("dora", response.getBody().getUsername());
        verify(roleUpdateRequestService).createRequest("dora", createDto);
    }

    @Test
    void listMyRoleUpdateRequests_usesAuthenticatedSubject() {
        Jwt jwt = jwt("dora");
        RoleUpdateRequestDto responseDto = new RoleUpdateRequestDto();
        when(roleUpdateRequestService.listRequestsForUser("dora")).thenReturn(List.of(responseDto));

        ResponseEntity<List<RoleUpdateRequestDto>> response =
                controller.listMyRoleUpdateRequests(jwt);

        assertEquals(List.of(responseDto), response.getBody());
    }

    private Jwt jwt(final String subject) {
        return Jwt.withTokenValue("token").header("alg", "none").subject(subject).build();
    }
}
