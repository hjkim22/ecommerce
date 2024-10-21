package com.ecommerce.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(CustomException.class) // CustomException 발생 시
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
    logger.error("Custom exception occurred: {}", e.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(e.getStatusCode(), e.getMessage());
    return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
  }

  @ExceptionHandler(NullPointerException.class) // NullPointerException 발생 시
  public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException e) {
    logger.error("Null pointer exception: {}", e.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error.");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }
}