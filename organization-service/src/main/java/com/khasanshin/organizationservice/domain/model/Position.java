package com.khasanshin.organizationservice.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Position {
    UUID id;
    String name;
    Instant createdAt;
    Instant updatedAt;
}
