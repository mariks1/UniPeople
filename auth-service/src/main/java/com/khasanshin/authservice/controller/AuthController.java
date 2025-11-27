package com.khasanshin.authservice.controller;

import com.khasanshin.authservice.dto.LoginRequestDto;
import com.khasanshin.authservice.dto.TokenDto;
import com.khasanshin.authservice.entity.AppUser;
import com.khasanshin.authservice.service.TokenService;
import com.khasanshin.authservice.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public TokenDto login(@Valid @RequestBody LoginRequestDto dto) {
        AppUser u;
        try {
            u = userService.findByUsername(dto.getUsername());
        } catch (EntityNotFoundException ex) {
            throw new BadCredentialsException("Bad credentials");
        }
        if (!u.isEnabled() || !passwordEncoder.matches(dto.getPassword(), u.getPasswordHash())) {
            throw new BadCredentialsException("Bad credentials");
        }
        var token = tokenService.issue(u);;
        var exp = Instant.now().plus(Duration.ofHours(1));
        return TokenDto.builder().accessToken(token).expiresAt(exp).build();
    }
}
