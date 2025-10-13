package temp.unipeople.common.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalStateException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public Map<String, Object> handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
    String msg = Optional.ofNullable(ex.getMessage()).orElse("Illegal state");
    log.debug("Illegal state at {}: {}", req.getRequestURI(), msg);
    return body(HttpStatus.CONFLICT, "IllegalState", msg, req, null);
  }

  @ExceptionHandler(PropertyReferenceException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handlePropertyReference(
      PropertyReferenceException ex, HttpServletRequest req) {

    TypeInformation<?> ti = ex.getType();
    ti.getType();
    String entity = ti.getType().getSimpleName();
    String prop = Optional.of(ex.getPropertyName()).orElse("unknown");

    String msg = "Unknown property '%s' for '%s'".formatted(prop, entity);

    Map<String, Object> details = new LinkedHashMap<>();
    details.put("entity", entity);
    details.put("property", prop);
    details.put("reason", Optional.of(ex.getMessage()).orElse(""));

    log.debug("Property reference error at {}: {} -> {}", req.getRequestURI(), msg, details);
    return body(HttpStatus.BAD_REQUEST, "BadProperty", msg, req, details);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest req) {
    Map<String, List<String>> fields =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.groupingBy(
                    FieldError::getField,
                    LinkedHashMap::new,
                    Collectors.mapping(
                        fe -> Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value"),
                        Collectors.toList())));
    log.debug("Validation failed at {} -> {}", req.getRequestURI(), fields);
    return body(
        HttpStatus.BAD_REQUEST,
        "ValidationFailed",
        "Validation failed",
        req,
        Map.of("fields", fields));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest req) {
    Map<String, List<String>> fields =
        ex.getConstraintViolations().stream()
            .collect(
                Collectors.groupingBy(
                    cv -> cv.getPropertyPath().toString(),
                    LinkedHashMap::new,
                    Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())));
    log.debug("Constraint violation at {} -> {}", req.getRequestURI(), fields);
    return body(
        HttpStatus.BAD_REQUEST,
        "ConstraintViolation",
        "Validation failed",
        req,
        Map.of("fields", fields));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleUnreadable(
      HttpMessageNotReadableException ex, HttpServletRequest req) {
    log.debug(
        "Bad request body at {}: {}", req.getRequestURI(), ex.getMostSpecificCause().getMessage());
    return body(HttpStatus.BAD_REQUEST, "BadRequest", "Malformed request body", req, null);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
    String msg = "Parameter '%s' has invalid value '%s'".formatted(ex.getName(), ex.getValue());
    log.debug("Type mismatch at {}: {}", req.getRequestURI(), msg);
    return body(HttpStatus.BAD_REQUEST, "TypeMismatch", msg, req, null);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleMissingParam(
      MissingServletRequestParameterException ex, HttpServletRequest req) {
    String msg = "Missing required parameter: " + ex.getParameterName();
    return body(HttpStatus.BAD_REQUEST, "MissingParameter", msg, req, null);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Map<String, Object> handleNotFound(EntityNotFoundException ex, HttpServletRequest req) {
    return body(HttpStatus.NOT_FOUND, "NotFound", ex.getMessage(), req, null);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest req) {
    return body(HttpStatus.BAD_REQUEST, "BadRequest", ex.getMessage(), req, null);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public Map<String, Object> handleConflict(
      DataIntegrityViolationException ex, HttpServletRequest req) {
    log.info(
        "Data integrity violation at {}: {}",
        req.getRequestURI(),
        Optional.of(ex.getMostSpecificCause()).map(Throwable::getMessage).orElse(ex.getMessage()));
    return body(HttpStatus.CONFLICT, "Conflict", "Data integrity violation", req, null);
  }

  @ExceptionHandler(TransactionSystemException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleTx(
      TransactionSystemException ignoredEx, HttpServletRequest req) {
    return body(HttpStatus.BAD_REQUEST, "BadRequest", "Validation failed", req, null);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Map<String, Object> handleNoHandler(
      NoHandlerFoundException ignoredEx, HttpServletRequest req) {
    return body(HttpStatus.NOT_FOUND, "NotFound", "Endpoint not found", req, null);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  public Map<String, Object> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
    return body(HttpStatus.METHOD_NOT_ALLOWED, "MethodNotAllowed", ex.getMessage(), req, null);
  }

  // ---------- FALLBACK ----------

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Map<String, Object> handleAny(Exception ex, HttpServletRequest req) {
    log.error("Unhandled error at {}: {}", req.getRequestURI(), ex.getMessage(), ex);
    return body(HttpStatus.INTERNAL_SERVER_ERROR, "Internal", "Internal server error", req, null);
  }

  // ---------- helpers ----------

  private Map<String, Object> body(
      HttpStatus status,
      String code,
      String message,
      HttpServletRequest req,
      Map<String, ?> extra) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("error", code);
    m.put("status", status.value());
    m.put("message", message);
    m.put("path", req.getRequestURI());
    m.put("timestamp", Instant.now().toString());
    String cid = MDC.get("cid");
    if (cid != null) m.put("correlationId", cid);
    if (extra != null && !extra.isEmpty()) m.putAll(extra);
    return m;
  }
}
