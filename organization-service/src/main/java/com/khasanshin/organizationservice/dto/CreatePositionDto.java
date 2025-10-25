package com.khasanshin.organizationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class CreatePositionDto {

  @NotBlank
  @Size(max = 150)
  String name;
}
