package com.khasanshin.employmentservice.infrastructure.external;

import com.khasanshin.employmentservice.domain.port.EmployeeVerifierPort;
import com.khasanshin.employmentservice.feign.EmployeeVerifier;
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
