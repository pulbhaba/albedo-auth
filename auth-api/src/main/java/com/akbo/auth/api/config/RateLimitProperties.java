package com.akbo.auth.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth.rate-limit")
public class RateLimitProperties {
    private int maxRequests = 5;
    private Duration window = Duration.ofMinutes(1);
}
