package com.akbo.auth.api.config;

import com.akbo.auth.api.service.impl.FederatedIdentityOAuth2UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import org.springframework.security.oauth2.core.OAuth2AccessToken;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties", properties = {
        "app.auth.social-login.enabled=true",
        "app.auth.social-login.providers.google.enabled=false",
        "app.auth.social-login.providers.microsoft.enabled=false"
})
public class SocialLoginConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FederatedIdentityOAuth2UserService federatedIdentityOAuth2UserService;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    public void whenProviderDisabled_thenLoadUserThrowsException() {
        ClientRegistration googleRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("id")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationUri("uri")
                .redirectUri("uri")
                .tokenUri("uri")
                .build();
        
        OAuth2AccessToken token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token", Instant.now(), Instant.now().plusSeconds(3600));
        OAuth2UserRequest request = new OAuth2UserRequest(googleRegistration, token);
        
        assertThrows(OAuth2AuthenticationException.class, () -> {
            federatedIdentityOAuth2UserService.loadUser(request);
        });
    }

    @Test
    public void whenMicrosoftDisabled_thenLoadUserThrowsException() {
        ClientRegistration microsoftRegistration = ClientRegistration.withRegistrationId("microsoft")
                .clientId("id")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationUri("uri")
                .redirectUri("uri")
                .tokenUri("uri")
                .build();

        OAuth2AccessToken token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token", Instant.now(), Instant.now().plusSeconds(3600));
        OAuth2UserRequest request = new OAuth2UserRequest(microsoftRegistration, token);

        assertThrows(OAuth2AuthenticationException.class, () -> {
            federatedIdentityOAuth2UserService.loadUser(request);
        });
    }

    @Test
    public void whenSocialLoginEnabled_thenOAuth2LoginConfigured() throws Exception {
        ClientRegistration googleRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("id")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationUri("uri")
                .redirectUri("uri")
                .tokenUri("uri")
                .build();
        when(clientRegistrationRepository.findByRegistrationId(anyString())).thenReturn(googleRegistration);

        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection());
    }
}
