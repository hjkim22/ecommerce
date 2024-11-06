package com.ecommerce.common.config;

import com.ecommerce.common.security.JwtAuthenticationFilter;
import com.ecommerce.common.security.JwtTokenArgumentResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig implements WebMvcConfigurer {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtTokenArgumentResolver jwtTokenArgumentResolver;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .httpBasic(AbstractHttpConfigurer::disable) // 기본 HTTP 인증 비활성화
        .csrf(AbstractHttpConfigurer::disable)      // CSRF 보호 비활성화 (JWT 사용 시 필요)
        .sessionManagement(sessionManagement ->
            sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 상태 없는 세션 관리
        .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/v1/members/sign-up", "/api/v1/members/sign-in") // 회원가입 및 로그인 허용
                .permitAll()                  // 위의 요청은 누구나 접근 가능
//            .anyRequest().authenticated() // 나머지 요청은 인증 필요
                .anyRequest().permitAll() // 테스트용
        )
        .addFilterBefore(jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class); // JWT 필터 추가

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // @JwtToken 어노테이션을 사용한 메서드 파라미터가 자동으로 JWT 토큰을 추출하고 필요한 정보를 바인딩
  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(jwtTokenArgumentResolver);
  }
}
