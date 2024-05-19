package com.akbo.auth.api;


import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.akbo.auth.dao.entity.UserRole;
import com.akbo.auth.dao.repository.UserRoleRepository;
import com.akbo.auth.dto.Role;

@SpringBootApplication
@EntityScan(basePackages = { "com.akbo.auth" })
@ComponentScan(basePackages = { "com.akbo.auth" })
@EnableJpaRepositories(basePackages = { "com.akbo.auth" })
public class AuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}

	@Bean
    public CommandLineRunner demoData(UserRoleRepository userRoleRepository) {
        return args -> {
			Arrays.stream(Role.values()).forEach(role -> userRoleRepository.save(new UserRole(role)));
        };
    }

}
