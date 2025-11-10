package com.khasanshin.employmentservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class CreateEmploymentDto {

  @NotNull
  @JsonProperty("employee_id")
  @JsonAlias("employee_id")
  UUID employeeId;

  @NotNull
  @JsonProperty("department_id")
  @JsonAlias("department_id")
  UUID departmentId;

  @NotNull
  @JsonProperty("position_id")
  @JsonAlias("position_id")
  UUID positionId;

  @NotNull
  @JsonProperty("start_date")
  @JsonAlias("start_date")
  LocalDate startDate;

  @DecimalMin(value = "0.01")
  @DecimalMax(value = "2.00")
  BigDecimal rate;

  @PositiveOrZero Integer salary;
}
