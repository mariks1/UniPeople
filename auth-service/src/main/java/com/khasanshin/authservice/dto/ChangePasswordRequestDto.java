package com.khasanshin.authservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class ChangePasswordRequestDto {

    @NotBlank
    @Size(min = 6, max = 128)
    @JsonProperty("new_password")
    private final String newPassword;
}
