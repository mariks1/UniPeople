package com.khasanshin.leaveservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class UpdateLeaveTypeDto {

  @Pattern(regexp = "^(?=.*\\S).+$")
  @Size(max = 64)
  String code;

  @Pattern(regexp = "^(?=.*\\S).+$")
  @Size(max = 150)
  String name;

  Boolean paid;

  @Positive
  @JsonAlias("max_days_per_year")
  @JsonProperty("max_days_per_year")
  Integer maxDaysPerYear;
}
