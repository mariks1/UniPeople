package com.khasanshin.authservice.controller;

import com.khasanshin.authservice.application.AuthUseCase;
import com.khasanshin.authservice.dto.LoginRequestDto;
import com.khasanshin.authservice.dto.TokenDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/login")
    public TokenDto login(@Valid @RequestBody LoginRequestDto dto) {
        return authUseCase.login(dto);
    }
}
