package com.khasanshin.dutyservice.domain.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Duty {
    UUID id;
    String code;
    String name;
}
