package com.ecommerce.domain.member;

import com.ecommerce.common.entity.BaseTimeEntity;
import com.ecommerce.common.enums.Role;
import com.ecommerce.domain.cart.CartEntity;
import com.ecommerce.domain.product.ProductEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

  @NotNull
  @Enumerated(EnumType.STRING)
  private Role role;

  // ProductEntity 와의 관계 설정 / MemberEntity 삭제 시 관련된 ProductEntity 함께 삭제
  @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProductEntity> products;

  // CartEntity 와의 관계 설정 / MemberEntity 삭제 시 관련된 CartEntity 함께 삭제
  @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
  private CartEntity cart;

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
