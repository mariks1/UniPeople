package com.khasanshin.employmentservice.domain.port;

import java.util.UUID;

public interface OrgVerifierPort {

    void ensureDepartmentExists(UUID departmentId);

    void ensurePositionExists(UUID positionId);
}
