package com.khasanshin.organizationservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class PositionDto {

  UUID id;
  String name;

  @JsonAlias("created_at")
  @JsonProperty("created_at")
  Instant createdAt;

  @JsonAlias("updated_at")
  @JsonProperty("updated_at")
  Instant updatedAt;
}
