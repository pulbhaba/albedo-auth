package com.akbo.auth.api.config;

import com.akbo.auth.util.PasswordTools;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Slf4j
@Configuration
public class Config {

    @Value("${spring.security.client.secret}")
    private String encryptSecret;

    @Value("${spring.security.client.salt}")
    private String encryptSalt;

    @Bean
    public ModelMapper getModelMapper() {
        log.info("Model mapper bean is creating.");
        return new ModelMapper();
    }

    @Bean
    public SecretKey getSymmetricKey() {
        log.info("Symmetric key bean is creating.");
        return PasswordTools.getKeyFromPassword(encryptSecret, encryptSalt);
    }
}
