package com.akbo.auth.api.security;

import com.akbo.auth.api.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PublicEndpointRateLimitFilter extends OncePerRequestFilter {
    private static final String REGISTER_PATH = "/public/user/register";
    private static final String RESET_PATH_SUFFIX = "/reset-password";

    private final RateLimitProperties properties;
    private final Map<String, RequestWindow> windows = new ConcurrentHashMap<>();

    public PublicEndpointRateLimitFilter(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String endpoint = endpointKey(request);
        if (endpoint != null && !allow(request.getRemoteAddr() + endpoint)) {
            response.setStatus(429);
            response.setHeader("Retry-After", Long.toString(retryAfterSeconds()));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Too many requests\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String endpointKey(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (REGISTER_PATH.equals(path) && "POST".equalsIgnoreCase(request.getMethod())) {
            return ":register";
        }
        if (path.startsWith("/public/user/")
                        && path.endsWith(RESET_PATH_SUFFIX)
                        && "GET".equalsIgnoreCase(request.getMethod())) {
            return ":password-reset";
        }
        return null;
    }

    private boolean allow(String clientKey) {
        long now = System.nanoTime();
        long windowNanos = properties.getWindow().toNanos();
        RequestWindow window =
                windows.compute(
                        clientKey,
                        (key, current) -> {
                            if (current == null || now - current.startedAtNanos >= windowNanos) {
                                return new RequestWindow(now, 1);
                            }
                            current.requests++;
                            return current;
                        });
        return window.requests <= properties.getMaxRequests();
    }

    private long retryAfterSeconds() {
        Duration window = properties.getWindow();
        return Math.max(1, (window.toMillis() + 999) / 1000);
    }

    private static final class RequestWindow {
        private final long startedAtNanos;
        private int requests;

        private RequestWindow(long startedAtNanos, int requests) {
            this.startedAtNanos = startedAtNanos;
            this.requests = requests;
        }
    }
}
