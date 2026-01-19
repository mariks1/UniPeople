package com.khasanshin.employeeservice.domain.port;

import java.util.UUID;

public interface OrgVerifierPort {

    void ensureDepartmentExists(UUID departmentId);

    void clearHeadByEmployee(UUID employeeId);
}
