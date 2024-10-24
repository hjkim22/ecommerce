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
  private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 1시간

  @Value("${spring.jwt.secret}")
  private String secretKey;

  /**
   * JWT 토큰 생성
   * @param email 사용자 이메일
   * @param role 사용자 역할
   * @return 생성된 JWT 토큰
   */
  public String generateToken(String email, Role role) {
    return Jwts.builder()
        .setSubject(email) // 토큰 주체(이메일) 설정
        .claim(KEY_ROLES, role.name()) // 사용자 역할에 클레임 추가
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRE_TIME))
        .signWith(SignatureAlgorithm.HS256, this.secretKey)
        .compact();
  }

  /**
   * JWT 토큰에서 인증 정보 추출
   * @param token JWT 토큰
   * @return 인증 정보
   */
  public Authentication getAuthentication(String token) {
    String email = getUserEmailFromToken(token); // 토큰에서 이메일 추출
    Role role = Role.valueOf(parseClaims(token).get(KEY_ROLES).toString()); // 토큰에서 역할 추출

    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role.name());
    // 스프링 시큐리티의 User 객체 생성 (여기서는 비밀번호는 빈 값으로 설정)
    User principal = new User(email, "", Collections.singletonList(authority));

    // UsernamePasswordAuthenticationToken 객체를 생성하여 인증 정보를 반환
    return new UsernamePasswordAuthenticationToken(principal, token,
        Collections.singletonList(authority));
  }

  /**
   * JWT 토큰에서 사용자 이메일 추출
   * @param token JWT 토큰
   * @return 사용자 이메일
   */
  public String getUserEmailFromToken(String token) {
    return parseClaims(token).getSubject(); // 토큰에서 주체(이메일) 추출
  }

  /**
   * JWT 토큰 유효성 검사
   * @param token JWT 토큰
   * @return boolean 값
   */
  public boolean validateToken(String token) {
    try {
      Claims claims = parseClaims(token);
      return !claims.getExpiration().before(new Date()); // 만료 여부 확인
    } catch (Exception e) {
      log.error("Invalid JWT token: {}", e.getMessage());
      return false;
    }
  }

  /**
   * JWT 토큰에서 클레임 파싱 후 반환
   * @param token JWT 토큰
   * @return 파싱된 클레임
   */
  private Claims parseClaims(String token) {
    return Jwts.parser()
        .setSigningKey(this.secretKey)
        .parseClaimsJws(token)
        .getBody(); // 클레임 본문 반환
  }

  /**
   * HTTP 요청에서 JWT 토큰 추출
   * @param request HTTP 요청
   * @return JWT 토큰, 없으면 null
   */
  public String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7); // "Bearer " 부분 제거하고 토큰 값만 반환
    }
    return null;
  }
}
