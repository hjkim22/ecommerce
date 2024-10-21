package com.ecommerce.common.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
  CUSTOMER,
  SELLER;

  @Override
  public String getAuthority() {
    return "ROLE_" + name(); // 접두사 "ROLE_"를 추가하여 반환
  }
}
