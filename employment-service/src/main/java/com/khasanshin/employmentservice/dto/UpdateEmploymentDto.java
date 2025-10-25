package com.khasanshin.employmentservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class UpdateEmploymentDto {

  @DecimalMin(value = "0.01")
  @DecimalMax(value = "2.0")
  BigDecimal rate;

  @PositiveOrZero Integer salary;

  @JsonAlias("end_date")
  @JsonProperty("end_date")
  LocalDate endDate;
}
