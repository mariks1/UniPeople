package com.khasanshin.employeeservice.exception;

public class RemoteServiceUnavailableException extends RuntimeException {
    public RemoteServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
