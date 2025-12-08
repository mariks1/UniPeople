package com.khasanshin.authservice;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khasanshin.authservice.controller.JwksController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.web.servlet.MockMvc;


import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = JwksController.class)
@Import({
        JwksControllerTest.MethodSec.class,
        JwksControllerTest.KeysCfg.class,
        JwksControllerTest.TestSecurity.class
})
class JwksControllerTest {


    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSec { }

    @TestConfiguration
    static class KeysCfg {
        @Bean
        RSAPublicKey publicKey() throws Exception {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            return (RSAPublicKey) kp.getPublic();
        }
    }

    @TestConfiguration
    static class TestSecurity {
        @Bean
        org.springframework.security.web.SecurityFilterChain testChain(
                org.springframework.security.config.annotation.web.builders.HttpSecurity http
        ) throws Exception {
            return http
                    .csrf(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable)
                    .httpBasic(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable)
                    .formLogin(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(reg -> reg
                            .requestMatchers("/.well-known/jwks.json").permitAll()
                            .anyRequest().permitAll()
                    )
                    .build();
        }
    }

    @Test
    void jwks_returnsKeySetWithKidMain() throws Exception {
        var res = mvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();


        JsonNode root = mapper.readTree(res.getResponse().getContentAsByteArray());
        assertThat(root.has("keys")).isTrue();
        assertThat(root.get("keys").isArray()).isTrue();
        assertThat(root.get("keys").size()).isGreaterThanOrEqualTo(1);
        assertThat(root.get("keys").get(0).get("kid").asText()).isEqualTo("main");
        assertThat(root.get("keys").get(0).has("kty")).isTrue();
        assertThat(root.get("keys").get(0).has("n")).isTrue();
        assertThat(root.get("keys").get(0).has("e")).isTrue();
    }
}

