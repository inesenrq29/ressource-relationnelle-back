package com.ienrique.ressourceRelationnelle.exception;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            Map.of(
                "timestamp",
                Instant.now().toString(),
                "status",
                400,
                "error",
                "Bad Request",
                "message",
                ex.getMessage()));
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            Map.of(
                "timestamp",
                Instant.now().toString(),
                "status",
                404,
                "error",
                "Not Found",
                "message",
                ex.getMessage()));
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(
            Map.of(
                "timestamp",
                Instant.now().toString(),
                "status",
                403,
                "error",
                "Forbidden",
                "message",
                ex.getMessage()));
  }
}
