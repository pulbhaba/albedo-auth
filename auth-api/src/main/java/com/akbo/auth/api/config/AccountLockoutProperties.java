package com.akbo.auth.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth.account-lockout")
public class AccountLockoutProperties {

    private int maxFailedAttempts = 5;
    private Duration duration = Duration.ofMinutes(15);
}
