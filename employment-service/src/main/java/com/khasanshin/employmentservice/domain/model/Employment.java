package com.khasanshin.employmentservice.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Employment {
    UUID id;
    UUID employeeId;
    UUID departmentId;
    UUID positionId;
    LocalDate startDate;
    LocalDate endDate;
    BigDecimal rate;
    Integer salary;
    Status status;
    Instant createdAt;
    Instant updatedAt;

    public enum Status {
        ACTIVE,
        CLOSED
    }
}
