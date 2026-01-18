package com.khasanshin.authservice.config;

import com.khasanshin.authservice.domain.model.User;
import com.khasanshin.authservice.domain.port.PasswordHasherPort;
import com.khasanshin.authservice.domain.port.UserRepositoryPort;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SeedConfig {

    @Bean
    CommandLineRunner seedSupervisor(UserRepositoryPort repo,
                                     PasswordHasherPort hasher,
                                     @Value("${security.supervisor.username}") String username,
                                     @Value("${security.supervisor.password}") String password) {
        return args -> {
            String normalized = username.trim().toLowerCase(Locale.ROOT);
            if (repo.existsByUsernameIgnoreCase(normalized)) {
                return;
            }
            User u = User.builder()
                    .username(normalized)
                    .roles(Set.of("SUPERVISOR"))
                    .enabled(true)
                    .passwordHash(hasher.hash(password))
                    .build();
            repo.save(u);
        };
    }
}
