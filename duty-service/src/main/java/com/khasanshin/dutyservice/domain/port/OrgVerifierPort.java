package com.khasanshin.dutyservice.domain.port;

import java.util.UUID;

public interface OrgVerifierPort {

    void ensureDepartmentExists(UUID departmentId);
}
