package com.inventoryflow.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String CODE = "code";

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(
      ResourceNotFoundException ex,
      HttpServletRequest request
  ) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, "NOT_FOUND");
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Map<String, Object>> handleBadRequest(
      BadRequestException ex,
      HttpServletRequest request
  ) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, "BAD_REQUEST");
  }

  @ExceptionHandler(InvalidOrderStateException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidOrderState(
      InvalidOrderStateException ex,
      HttpServletRequest request
  ) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), request, "INVALID_ORDER_STATE");
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<Map<String, Object>> handleConflict(
      ConflictException ex,
      HttpServletRequest request
  ) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), request, "CONFLICT");
  }

  @ExceptionHandler(InsufficientStockException.class)
  public ResponseEntity<Map<String, Object>> handleInsufficientStock(
      InsufficientStockException ex,
      HttpServletRequest request
  ) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), request, "INSUFFICIENT_STOCK");
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, Object>> handleAccessDenied(
      AccessDeniedException ex,
      HttpServletRequest request
  ) {
    return build(
        HttpStatus.FORBIDDEN,
        ex.getMessage() != null ? ex.getMessage() : "Forbidden",
        request,
        "FORBIDDEN"
    );
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationErrors(
      MethodArgumentNotValidException ex,
      HttpServletRequest request
  ) {
    List<String> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(this::formatFieldError)
        .collect(Collectors.toList());

    String first = errors.isEmpty() ? "Validation failed" : errors.get(0);

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now().toString());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Bad Request");
    body.put(CODE, "VALIDATION_FAILED");
    body.put("message", first);
    body.put("path", request.getRequestURI());
    body.put("details", errors);
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleOtherExceptions(
      Exception ex,
      HttpServletRequest request
  ) {
    // Don't leak internal details for production safety.
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request, "INTERNAL_ERROR");
  }

  private ResponseEntity<Map<String, Object>> build(
      HttpStatus status,
      String message,
      HttpServletRequest request,
      String code
  ) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now().toString());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put(CODE, code);
    body.put("message", message);
    body.put("path", request.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }

  private String formatFieldError(FieldError error) {
    return error.getField() + ": " + error.getDefaultMessage();
  }
}

