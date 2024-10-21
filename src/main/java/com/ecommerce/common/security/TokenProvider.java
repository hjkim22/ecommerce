package com.ecommerce.common.security;

import com.ecommerce.common.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TokenProvider {

  private static final String KEY_ROLES = "roles";
  private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60;

  @Value("${spring.jwt.secret}")
  private String secretKey;

  // JWT 토큰 생성
  public String generateToken(String email, Role role) {
    return Jwts.builder()
        .setSubject(email)
        .claim(KEY_ROLES, role.name())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRE_TIME))
        .signWith(SignatureAlgorithm.HS256, this.secretKey)
        .compact();
  }

  // 토큰에서 인증 정보 추출
  public Authentication getAuthentication(String token) {
    String email = getUserEmailFromToken(token);
    // 토큰에서 역할 정보를 추출하여 Role enum 으로 변환
    Role role = Role.valueOf(parseClaims(token).get(KEY_ROLES).toString());

    // 역할 정보를 기반으로 사용자 권한을 설정 (SimpleGrantedAuthority 는 스프링 시큐리티에서 권한을 나타냄)
    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role.name());

    // 스프링 시큐리티의 User 객체 생성 (여기서는 비밀번호는 빈 값으로 설정)
    User principal = new User(email, "", Collections.singletonList(authority));
    // UsernamePasswordAuthenticationToken 객체를 생성하여 인증 정보를 반환
    return new UsernamePasswordAuthenticationToken(principal, token,
        Collections.singletonList(authority));
  }

  // JWT 토큰에서 subject(사용자 이메일) 추출
  public String getUserEmailFromToken(String token) {
    return parseClaims(token).getSubject();
  }

  // JWT 토큰의 유효성을 검사 (만료 여부 등)
  public boolean validateToken(String token) {
    try {
      // 토큰의 클레임을 파싱하여 만료 시간이 현재 시간보다 이후인지 확인
      Claims claims = parseClaims(token);
      return !claims.getExpiration().before(new Date());
    } catch (Exception e) {
      // 예외 발생 시 (토큰이 유효하지 않으면) false 반환
      log.error("Invalid JWT token: {}", e.getMessage());
      return false;
    }
  }

  // JWT 토큰에서 클레임을 파싱하여 반환
  private Claims parseClaims(String token) {
    return Jwts.parser()
        .setSigningKey(this.secretKey)
        .parseClaimsJws(token)
        .getBody();
  }

  // 요청에서 토큰 추출
  // HTTP 요청 헤더에서 "Authorization" 헤더에 있는 JWT 토큰을 추출
  public String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7); // "Bearer "를 제거하고 토큰 값만 반환
    }
    return null;
  }
}
