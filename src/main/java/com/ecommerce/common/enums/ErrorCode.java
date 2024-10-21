package com.ecommerce.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

  // 사용자 관련 오류 코드
  USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "사용자를 찾을 수 없습니다."),
  INVALID_PASSWORD(HttpStatus.UNAUTHORIZED.value(), "비밀번호가 일치하지 않습니다."),
  EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT.value(), "이미 등록된 이메일입니다."),
  PHONE_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT.value(), "이미 등록된 전화번호입니다."),
  SHORT_PASSWORD(HttpStatus.BAD_REQUEST.value(), "비밀번호는 6자 이상이어야 합니다.");

  private final int statusCode;     // Http 상태 코드
  private final String description; // 오류 설명
}
