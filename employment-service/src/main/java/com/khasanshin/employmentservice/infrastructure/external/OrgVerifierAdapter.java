package com.khasanshin.employmentservice.infrastructure.external;

import com.khasanshin.employmentservice.domain.port.OrgVerifierPort;
import com.khasanshin.employmentservice.feign.OrgVerifier;
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
    public void ensurePositionExists(UUID positionId) {
        delegate.ensurePositionExists(positionId);
    }
}
