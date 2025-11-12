package com.khasanshin.authservice.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtConfig {

    @Bean
    @Profile("dev")
    public KeyPair devKeyPair() throws Exception {
        var kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }

    @Bean
    @Profile("!dev")
    public KeyPair keyPairFromPem(
            @Value("${jwt.public-key-pem}") String publicPem,
            @Value("${jwt.private-key-pem}") String privatePem
    ) throws Exception {
        return new KeyPair(loadPublic(publicPem), loadPrivate(privatePem));
    }

    @Bean
    public RSAPublicKey rsaPublicKey(KeyPair kp) {
        return (RSAPublicKey) kp.getPublic();
    }

    @Bean
    public RSAPrivateKey rsaPrivateKey(KeyPair kp) {
        return (RSAPrivateKey) kp.getPrivate();
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("main")
                .build();

        var jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
        return new NimbusJwtEncoder(jwkSource);
    }

    private RSAPublicKey loadPublic(String pem) throws Exception {
        String content = pem.replaceAll("-----BEGIN PUBLIC KEY-----|-----END PUBLIC KEY-----|\\s", "");
        byte[] decoded = java.util.Base64.getDecoder().decode(content);
        var spec = new java.security.spec.X509EncodedKeySpec(decoded);
        return (RSAPublicKey) java.security.KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private RSAPrivateKey loadPrivate(String pem) throws Exception {
        String content = pem.replaceAll("-----BEGIN (.*)PRIVATE KEY-----|-----END (.*)PRIVATE KEY-----|\\s", "");
        byte[] decoded = java.util.Base64.getDecoder().decode(content);
        var spec = new java.security.spec.PKCS8EncodedKeySpec(decoded);
        return (RSAPrivateKey) java.security.KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

}
