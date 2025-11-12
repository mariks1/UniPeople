package com.khasanshin.authservice.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@Jacksonized
public class UserDto {
    private final UUID id;
    private final String username;
    private final Set<String> roles;
    private final UUID employeeId;
    private final Set<UUID> managedDeptIds;
    private final boolean enabled;

}
