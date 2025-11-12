package com.khasanshin.authservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Data
@Builder
@Jacksonized
public class TokenDto {

    @JsonProperty("access_token")
    private final String accessToken;

    @Builder.Default
    @JsonProperty("token_type")
    private final String tokenType = "Bearer";

    @JsonProperty("expires_at")
    private final Instant expiresAt;

}