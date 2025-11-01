package com.akbo.auth.api.oauth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = com.akbo.auth.api.AuthApplication.class,
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration"
        }
)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class OAuthIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void jwksEndpoint_returnsKeySet() throws Exception {
        mockMvc.perform(get("/oauth2/jwks").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.keys").isArray())
                .andExpect(jsonPath("$.keys.length()").value(greaterThan(0)));
    }

    @Test
    void tokenEndpoint_clientCredentials_returnsAccessToken() throws Exception {
        mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic("test-client", "test-secret"))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("grant_type", "client_credentials")
                        .param("scope", "read"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", equalTo("invalid_client")));
    }

    @Test
    void authorizeEndpoint_withoutLogin_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/oauth2/authorize")
                        .param("response_type", "code")
                        .param("client_id", "test-client")
                        .param("redirect_uri", "http://127.0.0.1:8080/login/oauth2/code/test-client")
                        .param("scope", "read")
                        .param("state", "abc"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "john")
    void authorizeEndpoint_withLogin_redirectsBackWithCode() throws Exception {
        mockMvc.perform(get("/oauth2/authorize")
                        .param("response_type", "code")
                        .param("client_id", "test-client")
                        .param("redirect_uri", "http://127.0.0.1:8080/login/oauth2/code/test-client")
                        .param("scope", "read")
                        .param("state", "xyz"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", allOf(
                        containsString("http://127.0.0.1:8080/login/oauth2/code/test-client"),
                        containsString("code="),
                        containsString("state=xyz")
                )));
    }
}
