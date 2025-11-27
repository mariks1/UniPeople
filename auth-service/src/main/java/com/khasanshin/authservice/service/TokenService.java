package com.khasanshin.authservice.service;

import com.khasanshin.authservice.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder encoder;
    @Value("${security.jwt.issuer}") private String issuer;
    @Value("${security.jwt.audience}") private String audience;

    public String issue(AppUser u) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(u.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofHours(1)))
                .audience(List.of(audience))
                .claim("roles", u.getRoles() == null ? List.of() : u.getRoles());

        if (u.getEmployeeId() != null) {
            builder.claim("employeeId", u.getEmployeeId());
        }

        if (u.getManagedDeptIds() != null && !u.getManagedDeptIds().isEmpty()) {
            builder.claim("managedDeptIds", u.getManagedDeptIds());
        }

        JwtClaimsSet claims = builder.build();
        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

}
