package com.khasanshin.authservice.application;

import com.khasanshin.authservice.domain.model.User;
import com.khasanshin.authservice.domain.port.PasswordHasherPort;
import com.khasanshin.authservice.domain.port.TokenIssuerPort;
import com.khasanshin.authservice.domain.port.UserRepositoryPort;
import com.khasanshin.authservice.dto.LoginRequestDto;
import com.khasanshin.authservice.dto.TokenDto;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthApplicationService implements AuthUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort hasher;
    private final TokenIssuerPort tokenIssuer;

    @Override
    public TokenDto login(LoginRequestDto dto) {
        User user = userRepository.findByUsernameIgnoreCase(normalize(dto.getUsername()))
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));

        if (!user.isEnabled() || !hasher.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Bad credentials");
        }

        Instant exp = Instant.now().plus(Duration.ofHours(1));
        String token = tokenIssuer.issue(user);
        return TokenDto.builder()
                .accessToken(token)
                .expiresAt(exp)
                .build();
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
