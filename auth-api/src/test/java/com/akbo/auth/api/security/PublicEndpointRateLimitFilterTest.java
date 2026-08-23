package com.akbo.auth.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import com.akbo.auth.api.config.RateLimitProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

class PublicEndpointRateLimitFilterTest {
    private RateLimitProperties properties;
    private PublicEndpointRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        properties.setMaxRequests(2);
        properties.setWindow(Duration.ofMinutes(1));
        filter = new PublicEndpointRateLimitFilter(properties);
    }

    @Test
    void throttlesRegistrationRequestsPerClientIp() throws Exception {
        assertAllowed("/public/user/register", "POST");
        assertAllowed("/public/user/register", "POST");

        MockHttpServletResponse response = perform("/public/user/register", "POST");

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isEqualTo("60");
        assertThat(response.getContentAsString()).isEqualTo("{\"error\":\"Too many requests\"}");
    }

    @Test
    void limitsResetRequestsButLeavesOtherPublicEndpointsUnchanged() throws Exception {
        assertAllowed("/public/user/bob/reset-password", "GET");
        assertAllowed("/public/user/bob/reset-password", "GET");
        assertThat(perform("/public/user/bob/reset-password", "GET").getStatus()).isEqualTo(429);

        MockFilterChain chain = new MockFilterChain();
        MockHttpServletRequest request = request("/public/change-password/", "POST");
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        assertThat(chain.getRequest()).isSameAs(request);
    }

    private void assertAllowed(String path, String method) throws Exception {
        assertThat(perform(path, method).getStatus()).isEqualTo(200);
    }

    private MockHttpServletResponse perform(String path, String method) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request(path, method), response, new MockFilterChain());
        return response;
    }

    private MockHttpServletRequest request(String path, String method) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRemoteAddr("192.0.2.10");
        return request;
    }
}
