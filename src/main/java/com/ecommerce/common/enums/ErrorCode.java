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
  SHORT_PASSWORD(HttpStatus.BAD_REQUEST.value(), "비밀번호는 6자 이상이어야 합니다."),
  INVALID_ROLE(HttpStatus.BAD_REQUEST.value(), "유효하지 않은 역할입니다. 'CUSTOMER' 또는 'SELLER' 만 입력 가능합니다."),
  INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST.value(), "잘못된 인증 코드입니다."),
  VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST.value(), "인증 코드가 만료되었습니다.");

  private final int statusCode;     // Http 상태 코드
  private final String description; // 오류 설명
}
