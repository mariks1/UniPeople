package com.khasanshin.authservice.domain.port;

import com.khasanshin.authservice.domain.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsById(UUID id);
    void deleteById(UUID id);
    Page<User> findAll(Pageable pageable);
}
