package com.ecommerce.common.security;

import com.ecommerce.common.enums.ErrorCode;
import com.ecommerce.common.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class JwtTokenArgumentResolver implements HandlerMethodArgumentResolver {

  private final TokenProvider tokenProvider;

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    // @JwtToken 어노테이션이 붙어 있는 파라미터만 처리
    return parameter.getParameterAnnotation(JwtToken.class) != null;
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

    // NativeWebRequest 에서 HttpServletRequest 가져오기
    HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

    // TokenProvider 에서 extractToken 메서드 사용하여 JWT 토큰 추출
    String token = tokenProvider.extractToken(request); // 기존 extractToken 메서드를 호출

    if (token == null || token.isEmpty()) {
      throw new CustomException(ErrorCode.TOKEN_MISSING);
    }

    return tokenProvider.extractUserIdFromToken(token);
  }
}
