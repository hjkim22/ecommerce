package com.ecommerce.domain.entity;

import com.ecommerce.common.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberEntity extends BaseTimeEntity implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Email // 이메일 형식 검증
  @Column(unique = true)
  private String email;

  @NotBlank
  private String password;

  @NotBlank
  private String name;

  @NotBlank
  @Column(unique = true)
  private String phoneNumber;

  @NotBlank
  private String address;

  @NotBlank
  @Enumerated(EnumType.STRING)
  private Role role;

  // TODO
  // 사용자 여부 확인 메서드
  // 판매자 여부 확인 메서드

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // Role 에서 접두사 "ROLE_" 이미 추가함
    return List.of(new SimpleGrantedAuthority(role.name()));
  }

  @Override
  public String getUsername() {
    return this.email; // 사용자명으로 이메일 사용
  }

  // 아래 오버라이드 메서드들은 우선 모두 true
  // 계정 상태와 상관없이 항상 인증 가능
  // 요구사항 정리해보고 변경 예정
  @Override
  public boolean isAccountNonExpired() {
    return true; // 계정 만료 여부
  }

  @Override
  public boolean isAccountNonLocked() {
    return true; // 계정 잠금 여부
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true; // 자격 증명 만료 여부
  }

  @Override
  public boolean isEnabled() {
    return true; // 계정 활성화 여부
  }
}
