package com.khasanshin.authservice;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.khasanshin.authservice.controller.AuthController;
import com.khasanshin.authservice.dto.LoginRequestDto;
import com.khasanshin.authservice.dto.TokenDto;
import com.khasanshin.authservice.entity.AppUser;
import com.khasanshin.authservice.service.TokenService;
import com.khasanshin.authservice.service.UserService;
import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;


import static org.hamcrest.Matchers.*;
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
    UserService userService;
    @MockitoBean
    TokenService tokenService;
    @MockitoBean
    PasswordEncoder passwordEncoder;


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


        UUID uid = UUID.randomUUID();
        AppUser user = AppUser.builder()
                .id(uid)
                .username(username)
                .enabled(true)
                .passwordHash("ENC")
                .roles(Set.of("EMPLOYEE"))
                .build();


        given(userService.findByUsername(username)).willReturn(user);
        given(passwordEncoder.matches(password, "ENC")).willReturn(true);
        given(tokenService.issue(user)).willReturn("jwt-token");

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
    void login_unknownUser_yields401() throws Exception {
        given(userService.findByUsername("xxx")).willThrow(new EntityNotFoundException());

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


    @Test
    void login_disabledOrBadPassword_yields401() throws Exception {
        AppUser user = AppUser.builder().id(UUID.randomUUID()).username("u").enabled(true).passwordHash("ENC").build();
        given(userService.findByUsername("user")).willReturn(user);
        given(passwordEncoder.matches("badbad", "ENC")).willReturn(false);

        LoginRequestDto req = LoginRequestDto.builder()
                .username("user")
                .password("badbad")
                .build();

        mvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}