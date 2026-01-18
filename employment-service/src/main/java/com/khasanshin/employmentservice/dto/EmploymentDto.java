package com.khasanshin.employmentservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.khasanshin.employmentservice.domain.model.Employment;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class EmploymentDto {
  UUID id;

  @JsonProperty("employee_id")
  @JsonAlias("employee_id")
  UUID employeeId;

  @JsonProperty("department_id")
  @JsonAlias("department_id")
  UUID departmentId;

  @JsonProperty("position_id")
  @JsonAlias("position_id")
  UUID positionId;

  @JsonProperty("start_date")
  @JsonAlias("start_date")
  LocalDate startDate;

  @JsonProperty("end_date")
  @JsonAlias("end_date")
  LocalDate endDate;

  BigDecimal rate;
  Integer salary;
  Employment.Status status;

  @JsonProperty("created_at")
  @JsonAlias("created_at")
  Instant createdAt;

  @JsonProperty("updated_at")
  @JsonAlias("updated_at")
  Instant updatedAt;
}
