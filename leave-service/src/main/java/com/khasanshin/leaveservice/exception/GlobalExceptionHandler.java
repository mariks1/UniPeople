package com.khasanshin.leaveservice.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return pd(HttpStatus.BAD_REQUEST, "Invalid request", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        return pd(HttpStatus.BAD_REQUEST, "Business rule violation", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var pd = pd(HttpStatus.BAD_REQUEST, "Validation failed",
                "Request body contains invalid fields");
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String field = (err instanceof FieldError fe) ? fe.getField() : err.getObjectName();
            errors.put(field, err.getDefaultMessage());
        });
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ProblemDetail handleWebExchangeBind(WebExchangeBindException ex) {
        var pd = pd(HttpStatus.BAD_REQUEST, "Validation failed",
                "Request parameters contain invalid values");
        Map<String, String> errors = new HashMap<>();
        ex.getFieldErrors().forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));
        ex.getGlobalErrors().forEach(ge -> errors.put(ge.getObjectName(), ge.getDefaultMessage()));
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(
            AccessDeniedException ex,
            ServerWebExchange exchange
    ) {
        String path = exchange.getRequest().getURI().getPath();
        log.warn("Access denied for {}: {}", path, ex.getMessage());

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Access denied");
        pd.setDetail("Недостаточно прав для доступа к ресурсу");
        pd.setProperty("path", path);
        return pd;
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ProblemDetail handleServerWebInput(ServerWebInputException ex) {
        return pd(HttpStatus.BAD_REQUEST, "Malformed request",
                ex.getReason() != null ? ex.getReason() : "Failed to read/convert request");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        var pd = pd(HttpStatus.BAD_REQUEST, "Constraint violation", "One or more constraints failed");
        Map<String, String> violations = new HashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            violations.put(v.getPropertyPath().toString(), v.getMessage());
        }
        pd.setProperty("errors", violations);
        return pd;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        return pd(HttpStatus.CONFLICT, "Integrity constraint violation",
                "Unique/foreign key constraint failed");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatus(ResponseStatusException ex) {
        var pd = ProblemDetail.forStatus(ex.getStatusCode());
        pd.setTitle(ex.getStatusCode().toString());
        pd.setDetail(ex.getReason());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnhandled(Exception ex) {
        log.error("Unhandled exception", ex);
        return pd(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error", "Unexpected error occurred");
    }

    private static ProblemDetail pd(HttpStatus status, String title, String detail) {
        var pd = ProblemDetail.forStatus(status);
        pd.setTitle(title);
        pd.setDetail(detail);
        return pd;
    }
}