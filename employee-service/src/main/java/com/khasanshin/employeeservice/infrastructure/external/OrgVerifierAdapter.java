package com.khasanshin.employeeservice.infrastructure.external;

import com.khasanshin.employeeservice.domain.port.OrgVerifierPort;
import com.khasanshin.employeeservice.feign.OrgVerifier;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrgVerifierAdapter implements OrgVerifierPort {

    private final OrgVerifier delegate;

    @Override
    public void ensureDepartmentExists(UUID departmentId) {
        delegate.ensureDepartmentExists(departmentId);
    }

    @Override
    public void clearHeadByEmployee(UUID employeeId) {
        delegate.clearHeadByEmployee(employeeId);
    }
}
