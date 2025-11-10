package com.khasanshin.employmentservice.exception;

public class RemoteServiceUnavailableException extends RuntimeException {
    public RemoteServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
