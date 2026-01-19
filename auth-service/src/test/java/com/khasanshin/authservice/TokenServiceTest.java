package com.khasanshin.authservice;


import com.khasanshin.authservice.domain.model.User;
import com.khasanshin.authservice.infrastructure.security.JwtTokenIssuerAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;


import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    JwtEncoder encoder;

    JwtTokenIssuerAdapter service;

    @BeforeEach
    void setUp() {
        service = new JwtTokenIssuerAdapter(encoder);
        ReflectionTestUtils.setField(service, "issuer", "auth-service");
        ReflectionTestUtils.setField(service, "audience", "main-app");
    }

    @Test
    void issue_buildsClaimsAndReturnsTokenValue() {
        UUID userId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        Set<String> roles = Set.of("EMPLOYEE", "HR");
        Set<UUID> managed = Set.of(UUID.randomUUID(), UUID.randomUUID());


        User user = User.builder()
                .id(userId)
                .roles(roles)
                .employeeId(employeeId)
                .managedDeptIds(managed)
                .build();


        Jwt jwt = Jwt.withTokenValue("jwt-token")
                .headers(h -> h.put("alg", "none"))
                .claims(c -> c.put("dummy", true))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofMinutes(5)))
                .build();
        ArgumentCaptor<JwtEncoderParameters> paramsCaptor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        when(encoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        String token = service.issue(user);

        assertThat(token).isEqualTo("jwt-token");
        verify(encoder).encode(paramsCaptor.capture());

        Object claimsObj = ReflectionTestUtils.getField(paramsCaptor.getValue(), "claims");
        assertThat(claimsObj).isInstanceOf(JwtClaimsSet.class);
        JwtClaimsSet claims = (JwtClaimsSet) claimsObj;


        Assertions.assertNotNull(claims);
        Object iss = claims.getClaims().get("iss");
        assertThat(iss).as("issuer").isNotNull();
        assertThat(iss.toString()).isEqualTo("auth-service");


        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.getAudience()).containsExactly("main-app");
        Set<String> jwtRoles = claims.getClaim("roles");
        assertThat(jwtRoles).containsExactlyInAnyOrderElementsOf(roles);
        UUID empId = claims.getClaim("employeeId");
        assertThat(empId).isEqualTo(employeeId);
        Set<UUID> managedDeptsIds = claims.getClaim("managedDeptIds");
        assertThat(managedDeptsIds).containsExactlyInAnyOrderElementsOf(managed);
        assertThat(Duration.between(claims.getIssuedAt(), claims.getExpiresAt()).toMinutes()).isBetween(59L, 61L);
    }
}
