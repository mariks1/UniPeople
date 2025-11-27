package com.khasanshin.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class LoginRequestDto {

    @NotBlank
    @Size(min = 3, max = 64)
    private final String username;

    @NotBlank
    @Size(min = 6, max = 128)
    private final String password;
}