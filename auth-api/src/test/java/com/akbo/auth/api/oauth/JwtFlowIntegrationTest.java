package com.akbo.auth.api.oauth;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.akbo.auth.api.service.UserService;
import com.akbo.auth.dto.Role;
import com.akbo.auth.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.Set;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = com.akbo.auth.api.AuthApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@org.springframework.test.annotation.DirtiesContext
class JwtFlowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserService userService;
    @Autowired ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        UserDto user = new UserDto();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmailAddress("test@example.com");
        user.setRoles(Set.of(Role.ROLE_USER));
        userService.createUser(user);
    }

    @Test
    void fullJwtFlow_obtainingAndUsingToken() throws Exception {
        // 1. Obtain JWT token using password grant
        MvcResult tokenResult =
                mockMvc.perform(
                                post("/oauth2/token")
                                        .with(httpBasic("test-client", "test-secret"))
                                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                        .accept(MediaType.APPLICATION_JSON)
                                        .param("grant_type", "password")
                                        .param("username", "testuser")
                                        .param("password", "password")
                                        .param("scope", "read"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.access_token", not(emptyOrNullString())))
                        .andReturn();

        String responseContent = tokenResult.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
        String accessToken = (String) responseMap.get("access_token");

        // 2. Use the token to access a protected resource
        mockMvc.perform(
                        get("/user/testuser")
                                .header("Authorization", "Bearer " + accessToken)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", equalTo("testuser")));
    }

    @Test
    void logout_revokesAccessTokenAndRefreshToken() throws Exception {
        MvcResult tokenResult =
                mockMvc.perform(
                                post("/oauth2/token")
                                        .with(httpBasic("test-client", "test-secret"))
                                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                        .accept(MediaType.APPLICATION_JSON)
                                        .param("grant_type", "password")
                                        .param("username", "testuser")
                                        .param("password", "password")
                                        .param("scope", "read"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.access_token", not(emptyOrNullString())))
                        .andExpect(jsonPath("$.refresh_token", not(emptyOrNullString())))
                        .andReturn();

        String responseContent = tokenResult.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
        String accessToken = (String) responseMap.get("access_token");
        String refreshToken = (String) responseMap.get("refresh_token");

        mockMvc.perform(
                        post("/user/logout")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/user/testuser")
                                .header("Authorization", "Bearer " + accessToken)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(
                        post("/oauth2/token")
                                .with(httpBasic("test-client", "test-secret"))
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .accept(MediaType.APPLICATION_JSON)
                                .param("grant_type", "refresh_token")
                                .param("refresh_token", refreshToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", equalTo("invalid_grant")));
    }
}
