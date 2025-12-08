package com.khasanshin.fileservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.WebFilter;

@Slf4j
@Configuration
public class LoggingConfig {

    @Bean
    public WebFilter authLoggingFilter() {
        return (exchange, chain) -> {
            var req = exchange.getRequest();
            String auth = req.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            log.info("[REQ] {} {} Authorization: {}",
                    req.getMethod(),
                    req.getURI(),
                    auth != null ? (auth.substring(0, Math.min(auth.length(), 30)) + "...") : "null");

            return chain.filter(exchange);
        };
    }
}
