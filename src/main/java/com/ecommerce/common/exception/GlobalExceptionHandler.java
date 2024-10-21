package com.ecommerce.common.exception;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
    logger.error("Custom exception occurred: {}", e.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(e.getStatusCode(), e.getMessage());
    return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
  }

  @ExceptionHandler(NullPointerException.class)
  public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException e) {
    logger.error("Null pointer exception: {}", e.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error.");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  // 유효성 검사에서 실패 시
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
    String errorMessage = "Validation failed: " + Objects.requireNonNull(ex.getFieldError()).getDefaultMessage();
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT.value(), errorMessage);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
  }
}