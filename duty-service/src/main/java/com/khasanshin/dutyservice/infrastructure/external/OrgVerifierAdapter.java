package com.khasanshin.dutyservice.infrastructure.external;

import com.khasanshin.dutyservice.domain.port.OrgVerifierPort;
import com.khasanshin.dutyservice.feign.OrgVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrgVerifierAdapter implements OrgVerifierPort {

    private final OrgVerifier delegate;

    @Override
    public void ensureDepartmentExists(UUID departmentId) {
        delegate.ensureDepartmentExists(departmentId);
    }
}
