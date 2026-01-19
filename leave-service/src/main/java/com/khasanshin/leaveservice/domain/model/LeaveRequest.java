package com.khasanshin.leaveservice.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class LeaveRequest {

    public enum Status {
        DRAFT,
        PENDING,
        APPROVED,
        REJECTED,
        CANCELED
    }

    UUID id;
    UUID employeeId;
    UUID typeId;
    LocalDate dateFrom;
    LocalDate dateTo;
    Status status;
    UUID approverId;
    String comment;
    Instant createdAt;
    Instant updatedAt;
}
