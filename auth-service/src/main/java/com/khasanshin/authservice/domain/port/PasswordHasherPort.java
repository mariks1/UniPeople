package com.khasanshin.authservice.domain.port;

public interface PasswordHasherPort {
    String hash(String raw);
    boolean matches(String raw, String hashed);
}
