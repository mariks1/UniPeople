package com.khasanshin.authservice.service;


import com.khasanshin.authservice.dto.ChangePasswordRequestDto;
import com.khasanshin.authservice.dto.CreateUserRequestDto;
import com.khasanshin.authservice.dto.UpdateUserRolesRequestDto;
import com.khasanshin.authservice.dto.UserDto;
import com.khasanshin.authservice.entity.AppUser;
import com.khasanshin.authservice.mapper.UserMapper;
import com.khasanshin.authservice.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private static final Set<String> ALLOWED_ROLES = Set.of(
            "SUPERVISOR", "ORG_ADMIN", "HR", "DEPT_HEAD", "EMPLOYEE", "SYSTEM"
    );

    private final UserRepository repo;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

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

    private AppUser getOrThrow(UUID id) {
        return repo.findById(id).orElseThrow(() ->
                new EntityNotFoundException("User not found: " + id));
    }

    public UserDto create(CreateUserRequestDto dto) {
        String username = normalizeUsername(dto.getUsername());
        if (repo.existsByUsernameIgnoreCase(username)) {
            throw new DataIntegrityViolationException("Username already exists: " + username);
        }

        Set<String> roles = normalizeAndValidateRoles(dto.getRoles());

        AppUser entity = mapper.toEntity(dto);
        entity = entity.toBuilder()
                .username(username)
                .roles(roles)
                .build();

        entity.changePasswordHash(passwordEncoder.encode(dto.getPassword()));

        if (!entity.isEnabled() && Boolean.TRUE.equals(dto.getEnabled())) {
            entity = entity.toBuilder().enabled(true).build();
        } else if (dto.getEnabled() != null) {
            entity = entity.toBuilder().enabled(dto.getEnabled()).build();
        }

        AppUser saved = repo.save(entity);
        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public UserDto get(UUID id) {
        return mapper.toDto(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<UserDto> findAll(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toDto);
    }

    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("User not found: " + id);
        }
        repo.deleteById(id);
    }

    public UserDto setRoles(UUID id, UpdateUserRolesRequestDto dto) {
        AppUser appUser = getOrThrow(id);
        Set<String> roles = normalizeAndValidateRoles(dto.getRoles());
        appUser.setRoles(roles);
        return mapper.toDto(repo.save(appUser));
    }

    public UserDto setManagedDepartments(UUID id, Set<UUID> managedDeptIds) {
        AppUser appUser = getOrThrow(id);
        appUser.setManagedDeptIds(managedDeptIds == null ? Set.of() : managedDeptIds);
        return mapper.toDto(repo.save(appUser));
    }

    public void changePassword(UUID id, ChangePasswordRequestDto dto) {
        AppUser appUser = getOrThrow(id);
        appUser.changePasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        repo.save(appUser);
    }

    public UserDto setEnabled(UUID id, boolean enabled) {
        AppUser appUser = getOrThrow(id);
        appUser.setEnabled(enabled);
        return mapper.toDto(repo.save(appUser));
    }

    @Transactional(readOnly = true)
    public AppUser findByUsername(String username) {
        return repo.findByUsernameIgnoreCase(normalizeUsername(username))
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }
}
