package com.khasanshin.authservice.config;

import com.khasanshin.authservice.entity.AppUser;
import com.khasanshin.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class SeedConfig {

    @Bean
    CommandLineRunner seedSupervisor(UserRepository repo,
                                     PasswordEncoder pe,
                                     @org.springframework.beans.factory.annotation.Value("${security.supervisor.username:supervisor}") String username,
                                     @org.springframework.beans.factory.annotation.Value("${security.supervisor.password:change-me}") String password) {
        return args -> {
            String normalized = username.trim().toLowerCase(Locale.ROOT);
            if (repo.existsByUsernameIgnoreCase(normalized)) {
                return;
            }
            AppUser u = AppUser.builder()
                    .username(normalized)
                    .roles(Set.of("SUPERVISOR"))
                    .enabled(true)
                    .build();
            u.changePasswordHash(pe.encode(password));
            repo.save(u);
        };
    }
}
