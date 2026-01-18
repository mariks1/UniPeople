package com.khasanshin.organizationservice.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Department {
    UUID id;
    String code;
    String name;
    UUID facultyId;
    UUID headEmployeeId;
    Instant createdAt;
    Instant updatedAt;
}
