package com.khasanshin.dutyservice.dto;

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
public class DutyAssignmentDto {
  UUID id;

  @JsonProperty("department_id")
  @JsonAlias("department_id")
  UUID departmentId;

  @JsonProperty("employee_id")
  @JsonAlias("employee_id")
  UUID employeeId;

  @JsonProperty("duty_id")
  @JsonAlias("duty_id")
  UUID dutyId;

  @JsonProperty("assigned_by")
  @JsonAlias("assigned_by")
  UUID assignedBy;

  @JsonProperty("assigned_at")
  @JsonAlias("assigned_at")
  Instant assignedAt;

  String note;
}
