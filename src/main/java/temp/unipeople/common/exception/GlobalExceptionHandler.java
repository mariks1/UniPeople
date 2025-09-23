package temp.unipeople.common.exception;

import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.validation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, List<String>> fields =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.groupingBy(
                    FieldError::getField,
                    LinkedHashMap::new,
                    Collectors.mapping(
                        fe -> Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value"),
                        Collectors.toList())));
    return Map.of("error", "ValidationFailed", "fields", fields);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Map<String, Object> handleNotFound(EntityNotFoundException ex) {
    return Map.of("error", "NotFound", "message", ex.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public Map<String, Object> handleConflict(DataIntegrityViolationException ex) {
    return Map.of("error", "Conflict", "message", "Data integrity violation");
  }
}
