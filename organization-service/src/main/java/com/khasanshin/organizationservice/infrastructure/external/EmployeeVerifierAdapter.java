package com.khasanshin.organizationservice.infrastructure.external;

import com.khasanshin.organizationservice.domain.port.EmployeeVerifierPort;
import com.khasanshin.organizationservice.feign.EmployeeVerifier;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeVerifierAdapter implements EmployeeVerifierPort {

    private final EmployeeVerifier delegate;

    @Override
    public void ensureEmployeeExists(UUID employeeId) {
        delegate.ensureEmployeeExists(employeeId);
    }
}
