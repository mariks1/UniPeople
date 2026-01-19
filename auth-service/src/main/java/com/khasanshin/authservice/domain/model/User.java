package com.khasanshin.authservice.domain.model;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class User {

    UUID id;
    String username;
    String passwordHash;
    @Builder.Default
    Set<String> roles = Collections.emptySet();
    UUID employeeId;
    @Builder.Default
    Set<UUID> managedDeptIds = Collections.emptySet();
    @Builder.Default
    boolean enabled = true;
}
