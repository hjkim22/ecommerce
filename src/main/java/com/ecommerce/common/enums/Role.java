package com.ecommerce.common.enums;

import com.ecommerce.common.exception.CustomException;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
  CUSTOMER,
  SELLER,
  ADMIN;

  @Override
  public String getAuthority() {
    return "ROLE_" + name(); // 접두사 "ROLE_"를 추가하여 반환
  }

  @JsonCreator
  public static Role from(String role) {
    if (role == null || role.trim().isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_ROLE);
    }

    try {
      return Role.valueOf(role.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new CustomException(ErrorCode.INVALID_ROLE);
    }
  }
}
