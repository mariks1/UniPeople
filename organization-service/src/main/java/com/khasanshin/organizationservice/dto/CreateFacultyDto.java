package com.khasanshin.organizationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class CreateFacultyDto {

  @NotBlank
  @Size(max = 255)
  String name;

  @NotBlank
  @Size(max = 64)
  String code;
}
