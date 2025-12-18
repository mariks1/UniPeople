package com.khasanshin.authservice;

import com.khasanshin.authservice.config.JwtConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.oauth2.jwt.*;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = JwtConfig.class)
@ActiveProfiles("dev")
class JwtConfigTest {

    @Autowired
    JwtEncoder encoder;

    @Autowired
    RSAPublicKey rsaPublicKey;

    @Test
    void encoderProducesVerifiableToken() {
        var claims = JwtClaimsSet.builder()
                .issuer("https://auth.test")
                .subject("123")
                .audience(List.of("test-aud"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        String token = encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        JwtDecoder decoder = NimbusJwtDecoder.withPublicKey(rsaPublicKey).build();
        Jwt jwt = decoder.decode(token);

        assertThat(jwt.getSubject()).isEqualTo("123");
        assertThat(jwt.getIssuer().toString()).isEqualTo("https://auth.test");
        assertThat(jwt.getAudience()).contains("test-aud");
    }
}
