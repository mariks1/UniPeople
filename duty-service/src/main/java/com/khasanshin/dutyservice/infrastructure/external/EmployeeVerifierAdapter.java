package com.khasanshin.dutyservice.infrastructure.external;

import com.khasanshin.dutyservice.domain.port.EmployeeVerifierPort;
import com.khasanshin.dutyservice.feign.EmployeeVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EmployeeVerifierAdapter implements EmployeeVerifierPort {

    private final EmployeeVerifier delegate;

    @Override
    public void ensureEmployeeExists(UUID employeeId) {
        delegate.ensureEmployeeExists(employeeId);
    }
}
