package com.khasanshin.employeeservice.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Employee {

    UUID id;
    Integer version;
    String firstName;
    String lastName;
    String middleName;
    String workEmail;
    String phone;
    Status status;
    UUID department;
    Instant createdAt;
    Instant updatedAt;

    public enum Status {
        ACTIVE,
        FIRED
    }
}
