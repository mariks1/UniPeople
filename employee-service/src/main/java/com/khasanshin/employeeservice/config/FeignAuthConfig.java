package com.khasanshin.employeeservice.config;

import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
public class FeignAuthConfig {
    @Bean
    public feign.RequestInterceptor authRelay() {
        return template -> {
            var ctx = SecurityContextHolder.getContext();
            if (ctx != null && ctx.getAuthentication() instanceof JwtAuthenticationToken jwt) {
                template.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getToken().getTokenValue());
            }
        };
    }
}
