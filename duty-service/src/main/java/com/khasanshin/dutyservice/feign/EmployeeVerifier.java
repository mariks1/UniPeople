package com.khasanshin.dutyservice.feign;

import com.khasanshin.dutyservice.exception.RemoteServiceUnavailableException;
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
public class EmployeeVerifier {

    private final EmployeeClient employeeClient;

    @CircuitBreaker(name = "employeeClient", fallbackMethod = "remoteUnavailable")
    public void ensureEmployeeExists(UUID employeeId) {
        try {
            ResponseEntity<Void> resp = employeeClient.employeeExists(employeeId);
            if (resp == null || !resp.getStatusCode().is2xxSuccessful()) {
                throw new EntityNotFoundException("employeeId not found: " + employeeId);
            }
        } catch (FeignException.NotFound e) {
            throw new EntityNotFoundException("employeeId not found: " + employeeId);
        }
    }

    private void remoteUnavailable(UUID id, CallNotPermittedException ex) {
        throw new RemoteServiceUnavailableException("employee-service unavailable", ex);
    }
    private void remoteUnavailable(UUID id, RetryableException ex) {
        throw new RemoteServiceUnavailableException("employee-service unavailable", ex);
    }
    private void remoteUnavailable(UUID id, ConnectException ex) {
        throw new RemoteServiceUnavailableException("employee-service unavailable", ex);
    }
    private void remoteUnavailable(UUID id, SocketTimeoutException ex) {
        throw new RemoteServiceUnavailableException("employee-service unavailable", ex);
    }
    private void remoteUnavailable(UUID id, UnknownHostException ex) {
        throw new RemoteServiceUnavailableException("employee-service unavailable", ex);
    }


}
