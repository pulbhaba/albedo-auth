package com.akbo.auth.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.auth.social-login")
public class SocialLoginProperties {

    /** Whether social login is enabled globally. */
    private boolean enabled = true;

    /** Configuration per provider. */
    private Map<String, ProviderProperties> providers = new HashMap<>();

    @Data
    public static class ProviderProperties {
        /** Whether this provider is enabled. */
        private boolean enabled = true;
    }
}
