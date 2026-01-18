package com.khasanshin.employmentservice.domain.port;

import java.util.UUID;

public interface EmployeeVerifierPort {

    void ensureEmployeeExists(UUID employeeId);
}
