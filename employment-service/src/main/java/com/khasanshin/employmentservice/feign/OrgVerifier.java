package com.khasanshin.employmentservice.feign;

import com.khasanshin.employmentservice.exception.RemoteServiceUnavailableException;
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

    @CircuitBreaker(name = "positionClient", fallbackMethod = "positionUnavailable")
    public void ensurePositionExists(UUID positionId) {
        try {
            ResponseEntity<Void> resp = orgClient.positionExists(positionId);
            if (resp == null || !resp.getStatusCode().is2xxSuccessful()) {
                throw new EntityNotFoundException("position not found: " + positionId);
            }
        } catch (FeignException.NotFound e) {
            throw new EntityNotFoundException("position not found: " + positionId);
        }
    }

    private void positionUnavailable(UUID id, CallNotPermittedException ex) {
        throw new RemoteServiceUnavailableException("org-service unavailable", ex);
    }
    private void positionUnavailable(UUID id, RetryableException ex) {
        throw new RemoteServiceUnavailableException("org-service unavailable", ex);
    }
    private void positionUnavailable(UUID id, ConnectException ex) {
        throw new RemoteServiceUnavailableException("org-service unavailable", ex);
    }
    private void positionUnavailable(UUID id, SocketTimeoutException ex) {
        throw new RemoteServiceUnavailableException("org-service unavailable", ex);
    }
    private void positionUnavailable(UUID id, UnknownHostException ex) {
        throw new RemoteServiceUnavailableException("org-service unavailable", ex);
    }
}
