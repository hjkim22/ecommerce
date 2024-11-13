package com.ecommerce.common.security;

import com.ecommerce.common.repository.RedisCacheRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final TokenProvider tokenProvider;
  private final RedisCacheRepository redisCacheRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String token = tokenProvider.extractToken(request);

    // 토큰이 존재하고 유효한 경우
    if (StringUtils.hasText(token) && tokenProvider.isValidToken(token)) {
      Long userId = tokenProvider.extractUserIdFromToken(token);
      String cachedToken = redisCacheRepository.getData("user:session:" + userId);

      if (cachedToken != null && cachedToken.equals(token)) {
        Authentication authentication = tokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } else {
        // Redis 에 저장된 토큰과 요청한 토큰이 다르면 유효하지 않은 토큰 처리
        log.warn("Invalid token or token expired: {}", token);
      }
    }
    filterChain.doFilter(request, response);
  }
}
