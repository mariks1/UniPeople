package com.khasanshin.authservice.domain.port;

import com.khasanshin.authservice.domain.model.User;

public interface TokenIssuerPort {
    String issue(User user);
}
