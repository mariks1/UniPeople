package com.khasanshin.authservice;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khasanshin.authservice.application.AuthUseCase;
import com.khasanshin.authservice.controller.AuthController;
import com.khasanshin.authservice.dto.LoginRequestDto;
import com.khasanshin.authservice.dto.TokenDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AuthController.class)
@Import({AuthControllerTest.BadCredAdvice.class, AuthControllerTest.TestSecurity.class})
class AuthControllerTest {

    @TestConfiguration
    static class TestSecurity {
        @Bean
        SecurityFilterChain testChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
            return http
                    .httpBasic(AbstractHttpConfigurer::disable)
                    .formLogin(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/v1/auth/login").permitAll()
                            .anyRequest().authenticated()
                    )
                    .build();
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockitoBean
    AuthUseCase authUseCase;

    @RestControllerAdvice
    static class BadCredAdvice {
        @ExceptionHandler(BadCredentialsException.class)
        public org.springframework.http.ResponseEntity<?> onBadCred(BadCredentialsException ex) {
            return org.springframework.http.ResponseEntity.status(401).body(ex.getMessage());
        }
    }

    @Test
    void login_success_returnsTokenAndExpiry() throws Exception {
        var username = "john";
        var password = "secret";

        TokenDto resp = TokenDto.builder()
                .accessToken("jwt-token")
                .expiresAt(Instant.now().plus(Duration.ofHours(1)))
                .build();

        given(authUseCase.login(any(LoginRequestDto.class))).willReturn(resp);

        LoginRequestDto req = LoginRequestDto.builder()
                .username(username)
                .password(password)
                .build();

        Instant before = Instant.now();

        mvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token", is("jwt-token")))
                .andExpect(jsonPath("$.expires_at", notNullValue()))
                .andExpect(result -> {
                    TokenDto dto = mapper.readValue(result.getResponse().getContentAsByteArray(), TokenDto.class);
                    long minutes = Duration.between(before, dto.getExpiresAt()).toMinutes();
                    org.assertj.core.api.Assertions.assertThat(minutes).isBetween(59L, 61L);
                });
    }

    @Test
    void login_badCredentials_yields401() throws Exception {
        given(authUseCase.login(any(LoginRequestDto.class))).willThrow(new BadCredentialsException("Bad credentials"));

        LoginRequestDto req = LoginRequestDto.builder()
                .username("xxx")
                .password("yyyyyy")
                .build();

        mvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
