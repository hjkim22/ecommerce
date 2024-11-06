package com.ecommerce.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER) // 메서드 파라미터에서 적용
@Retention(RetentionPolicy.RUNTIME) // 실행중에 반영되도록
public @interface JwtToken {

}
