package com.khasanshin.employeeservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain chain(HttpSecurity http,
                              JwtDecoder decoder,
                              Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthConverter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health","/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(o -> o.jwt(j -> j
                        .decoder(decoder)
                        .jwtAuthenticationConverter(jwtAuthConverter)
                ));
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${security.jwt.issuer:https://auth.unipeople}") String issuer,
                          @Value("${security.jwt.audience:unipeople-api}") String audience,
                          @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwk) {
        var decoder = NimbusJwtDecoder.withJwkSetUri(jwk).build();
        var withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = jwt ->
                jwt.getAudience().contains(audience)
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token","Invalid audience",""));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));
        return decoder;
    }

    @Bean
    Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthConverter() {
        var gac = new JwtGrantedAuthoritiesConverter();
        gac.setAuthoritiesClaimName("roles");
        gac.setAuthorityPrefix("ROLE_");
        return jwt -> new JwtAuthenticationToken(jwt, gac.convert(jwt), jwt.getSubject());
    }
}
