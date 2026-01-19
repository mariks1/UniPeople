package com.khasanshin.authservice.application;

import com.khasanshin.authservice.dto.LoginRequestDto;
import com.khasanshin.authservice.dto.TokenDto;

public interface AuthUseCase {
    TokenDto login(LoginRequestDto dto);
}
