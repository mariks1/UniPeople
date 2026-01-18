package com.khasanshin.authservice.application;

import com.khasanshin.authservice.domain.model.User;
import com.khasanshin.authservice.domain.port.PasswordHasherPort;
import com.khasanshin.authservice.domain.port.UserRepositoryPort;
import com.khasanshin.authservice.dto.ChangePasswordRequestDto;
import com.khasanshin.authservice.dto.CreateUserRequestDto;
import com.khasanshin.authservice.dto.UpdateUserRolesRequestDto;
import com.khasanshin.authservice.dto.UserDto;
import com.khasanshin.authservice.mapper.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserApplicationService implements UserUseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of(
            "SUPERVISOR", "ORG_ADMIN", "HR", "DEPT_HEAD", "EMPLOYEE", "SYSTEM"
    );

    private final UserRepositoryPort repo;
    private final UserMapper mapper;
    private final PasswordHasherPort hasher;

    private String normalizeUsername(String raw) {
        return Objects.requireNonNull(raw, "username").trim().toLowerCase();
    }

    private Set<String> normalizeAndValidateRoles(Set<String> roles) {
        if (roles == null) return Set.of();
        Set<String> norm = roles.stream()
                .filter(Objects::nonNull)
                .map(r -> r.trim().toUpperCase())
                .collect(Collectors.toUnmodifiableSet());
        if (!ALLOWED_ROLES.containsAll(norm)) {
            Set<String> bad = norm.stream().filter(r -> !ALLOWED_ROLES.contains(r)).collect(Collectors.toSet());
            throw new IllegalArgumentException("Unknown roles: " + bad);
        }
        return norm;
    }

    private User getOrThrow(UUID id) {
        return repo.findById(id).orElseThrow(() ->
                new EntityNotFoundException("User not found: " + id));
    }

    @Override
    public UserDto create(CreateUserRequestDto dto) {
        String username = normalizeUsername(dto.getUsername());
        if (repo.existsByUsernameIgnoreCase(username)) {
            throw new DataIntegrityViolationException("Username already exists: " + username);
        }

        Set<String> roles = normalizeAndValidateRoles(dto.getRoles());
        String hashed = hasher.hash(dto.getPassword());

        User toSave = mapper.toDomain(dto, username, roles, hashed);
        User saved = repo.save(toSave);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto get(UUID id) {
        return mapper.toDto(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> findAll(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("User not found: " + id);
        }
        repo.deleteById(id);
    }

    @Override
    public UserDto setRoles(UUID id, UpdateUserRolesRequestDto dto) {
        User user = getOrThrow(id);
        Set<String> roles = normalizeAndValidateRoles(dto.getRoles());
        User updated = mapper.applyRoles(user, roles);
        return mapper.toDto(repo.save(updated));
    }

    @Override
    public UserDto setManagedDepartments(UUID id, Set<UUID> managedDeptIds) {
        User user = getOrThrow(id);
        User updated = mapper.applyManagedDepartments(user, managedDeptIds);
        return mapper.toDto(repo.save(updated));
    }

    @Override
    public void changePassword(UUID id, ChangePasswordRequestDto dto) {
        User user = getOrThrow(id);
        User updated = mapper.applyPasswordHash(user, hasher.hash(dto.getNewPassword()));
        repo.save(updated);
    }

    @Override
    public UserDto setEnabled(UUID id, boolean enabled) {
        User user = getOrThrow(id);
        User updated = mapper.applyEnabled(user, enabled);
        return mapper.toDto(repo.save(updated));
    }
}
