package com.khasanshin.authservice.mapper;

import com.khasanshin.authservice.domain.model.User;
import com.khasanshin.authservice.dto.CreateUserRequestDto;
import com.khasanshin.authservice.dto.UserDto;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) return null;
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .employeeId(user.getEmployeeId())
                .roles(user.getRoles())
                .managedDeptIds(user.getManagedDeptIds())
                .enabled(user.isEnabled())
                .build();
    }

    public User toDomain(CreateUserRequestDto dto, String normalizedUsername, Set<String> normalizedRoles, String hashedPassword) {
        return User.builder()
                .username(normalizedUsername)
                .passwordHash(hashedPassword)
                .roles(normalizedRoles)
                .employeeId(dto.getEmployeeId())
                .managedDeptIds(dto.getManagedDeptIds() == null ? Collections.emptySet() : dto.getManagedDeptIds())
                .enabled(Boolean.TRUE.equals(dto.getEnabled()))
                .build();
    }

    public User applyRoles(User user, Set<String> roles) {
        return user.toBuilder().roles(roles == null ? Collections.emptySet() : roles).build();
    }

    public User applyManagedDepartments(User user, Set<UUID> managed) {
        return user.toBuilder()
                .managedDeptIds(managed == null ? Collections.emptySet() : managed)
                .build();
    }

    public User applyEnabled(User user, boolean enabled) {
        return user.toBuilder().enabled(enabled).build();
    }

    public User applyPasswordHash(User user, String hashed) {
        Objects.requireNonNull(hashed, "hashed");
        return user.toBuilder().passwordHash(hashed).build();
    }
}
