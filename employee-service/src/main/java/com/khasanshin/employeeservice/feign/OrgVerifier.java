package com.khasanshin.employeeservice.feign;

import com.khasanshin.employeeservice.exception.RemoteServiceUnavailableException;
import feign.FeignException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrgVerifier {

    private final OrgClient orgClient;

    @CircuitBreaker(name = "orgClient", fallbackMethod = "ignoreClearHead")
    public void clearHeadByEmployee(UUID employeeId) {
        orgClient.clearHeadByEmployee(employeeId);
    }

    public void ignoreClearHead(UUID employeeId, Throwable cause) {}

    @CircuitBreaker(name = "departmentClient", fallbackMethod = "departmentUnavailable")
    public void ensureDepartmentExists(UUID departmentId) {
        try {
            ResponseEntity<Void> resp = orgClient.departmentExists(departmentId);

            if (resp == null || !resp.getStatusCode().is2xxSuccessful()) {
                throw new EntityNotFoundException("department not found: " + departmentId);
            }
        } catch (FeignException.NotFound e) {
            throw new EntityNotFoundException("department not found: " + departmentId);
        }
    }

    private void departmentUnavailable(UUID id, CallNotPermittedException ex) {
        throw new RemoteServiceUnavailableException("org-service unavailable", ex);
    }
    private void departmentUnavailable(UUID id, RetryableException ex) {
        throw new RemoteServiceUnavailableException("org-service unavailable", ex);
    }
    private void departmentUnavailable(UUID id, ConnectException ex) {
        throw new RemoteServiceUnavailableException("org-service unavailable", ex);
    }
    private void departmentUnavailable(UUID id, SocketTimeoutException ex) {
        throw new RemoteServiceUnavailableException("org-service unavailable", ex);
    }
    private void departmentUnavailable(UUID id, UnknownHostException ex) {
        throw new RemoteServiceUnavailableException("org-service unavailable", ex);
    }
}
