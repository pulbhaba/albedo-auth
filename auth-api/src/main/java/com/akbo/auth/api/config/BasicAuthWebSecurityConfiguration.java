package com.akbo.auth.api.config;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder;
import com.akbo.auth.api.service.UserService;
import com.akbo.auth.api.service.impl.FederatedIdentityOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SocialLoginProperties.class)
public class BasicAuthWebSecurityConfiguration {

    private final SocialLoginProperties socialLoginProperties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApplicationContext context)
            throws Exception {
        http.authorizeHttpRequests(
                        requests ->
                                requests.requestMatchers("/admin/**", "/user/**")
                                        .authenticated()
                                        .requestMatchers("/public/**", "/login/**", "/oauth2/**")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .httpBasic(withDefaults());

        if (socialLoginProperties.isEnabled()) {
            try {
                FederatedIdentityOAuth2UserService federatedIdentityOAuth2UserService =
                        context.getBean(FederatedIdentityOAuth2UserService.class);
                http.oauth2Login(
                        oauth2 ->
                                oauth2.userInfoEndpoint(
                                        userInfo ->
                                                userInfo.userService(
                                                        federatedIdentityOAuth2UserService)));
            } catch (Exception e) {
                // Social login is enabled but the required beans are not present (e.g. missing
                // oauth2 client config)
                // We just log it and proceed without social login
            }
        }

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, UserService userService)
            throws Exception {
        AuthenticationManagerBuilder builder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userService).passwordEncoder(passwordEncoder());
        return builder.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
