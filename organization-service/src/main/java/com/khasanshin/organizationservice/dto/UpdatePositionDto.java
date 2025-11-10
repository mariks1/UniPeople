package com.khasanshin.organizationservice.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class UpdatePositionDto {

  @Pattern(regexp = "^(?=.*\\S).+$", message = "name must contain a non-whitespace character")
  @Size(max = 150)
  String name;
}
