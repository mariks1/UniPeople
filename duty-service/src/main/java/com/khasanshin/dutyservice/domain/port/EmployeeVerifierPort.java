package com.khasanshin.dutyservice.domain.port;

import java.util.UUID;

public interface EmployeeVerifierPort {

    void ensureEmployeeExists(UUID employeeId);
}
