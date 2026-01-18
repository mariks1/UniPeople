package com.khasanshin.leaveservice.domain.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class LeaveType {
    UUID id;
    String code;
    String name;
    Boolean paid;
    Integer maxDaysPerYear;
}
