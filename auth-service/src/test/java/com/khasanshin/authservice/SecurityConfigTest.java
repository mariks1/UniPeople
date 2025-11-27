package com.khasanshin.authservice;

import com.khasanshin.authservice.config.SecurityConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    @Test
    void jwtAuthConverterBuildsAuthoritiesFromRolesClaim() {
        SecurityConfig config = new SecurityConfig();
        Converter<Jwt, ? extends AbstractAuthenticationToken> converter = config.jwtAuthConverter();

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of(
                        "sub", "user-id",
                        "roles", List.of("SUPERVISOR", "ORG_ADMIN")
                )
        );

        JwtAuthenticationToken auth = (JwtAuthenticationToken) converter.convert(jwt);

        Assertions.assertNotNull(auth);
        assertThat(auth.getName()).isEqualTo("user-id");
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_SUPERVISOR", "ROLE_ORG_ADMIN");
    }
}
