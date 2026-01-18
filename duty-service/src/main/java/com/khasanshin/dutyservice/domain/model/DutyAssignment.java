package com.khasanshin.dutyservice.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class DutyAssignment {
    UUID id;
    UUID departmentId;
    UUID employeeId;
    UUID dutyId;
    UUID assignedBy;
    Instant assignedAt;
    String note;
}
