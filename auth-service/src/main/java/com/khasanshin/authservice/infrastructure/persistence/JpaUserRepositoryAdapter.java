package com.khasanshin.authservice.infrastructure.persistence;

import com.khasanshin.authservice.domain.model.User;
import com.khasanshin.authservice.domain.port.UserRepositoryPort;
import com.khasanshin.authservice.infrastructure.persistence.entity.AppUserEntity;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repo;

    @Override
    public User save(User user) {
        return toDomain(repo.save(toEntity(user)));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsernameIgnoreCase(String username) {
        return repo.findByUsernameIgnoreCase(username).map(this::toDomain);
    }

    @Override
    public boolean existsByUsernameIgnoreCase(String username) {
        return repo.existsByUsernameIgnoreCase(username);
    }

    @Override
    public boolean existsById(UUID id) {
        return repo.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        repo.deleteById(id);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return repo.findAll(pageable).map(this::toDomain);
    }

    private User toDomain(AppUserEntity e) {
        if (e == null) return null;
        return User.builder()
                .id(e.getId())
                .username(e.getUsername())
                .passwordHash(e.getPasswordHash())
                .roles(defaultSet(e.getRoles()))
                .employeeId(e.getEmployeeId())
                .managedDeptIds(defaultSet(e.getManagedDeptIds()))
                .enabled(e.isEnabled())
                .build();
    }

    private AppUserEntity toEntity(User u) {
        if (u == null) return null;
        return AppUserEntity.builder()
                .id(u.getId())
                .username(u.getUsername())
                .passwordHash(u.getPasswordHash())
                .roles(defaultSet(u.getRoles()))
                .employeeId(u.getEmployeeId())
                .managedDeptIds(defaultSet(u.getManagedDeptIds()))
                .enabled(u.isEnabled())
                .build();
    }

    private <T> Set<T> defaultSet(Set<T> in) {
        return in == null ? Collections.emptySet() : in;
    }
}
