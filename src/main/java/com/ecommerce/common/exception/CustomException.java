package com.ecommerce.common.exception;

import com.ecommerce.common.enums.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

  private final ErrorCode errorCode;

  public CustomException(ErrorCode errorCode) {
    super(errorCode.getDescription());
    this.errorCode = errorCode;
  }

  // 오류 메시지도 받을 수 있도록 생성자 추가
  public CustomException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public int getStatusCode() {
    return errorCode.getStatusCode();
  }
}