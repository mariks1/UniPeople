package com.khasanshin.organizationservice.domain.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Faculty {
    UUID id;
    String code;
    String name;
}
