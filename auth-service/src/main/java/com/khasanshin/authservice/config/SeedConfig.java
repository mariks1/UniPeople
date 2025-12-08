package com.khasanshin.authservice.config;

import com.khasanshin.authservice.entity.AppUser;
import com.khasanshin.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
                                     @Value("${security.supervisor.username}") String username,
                                     @Value("${security.supervisor.password}") String password) {
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
            System.out.println("here lol");
            u.changePasswordHash(pe.encode(password));
            repo.save(u);
        };
    }
}
