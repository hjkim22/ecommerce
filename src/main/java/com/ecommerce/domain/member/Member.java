package com.ecommerce.domain.member;

import com.ecommerce.common.entity.BaseTimeEntity;
import com.ecommerce.common.enums.Role;
import com.ecommerce.domain.cart.Cart;
import com.ecommerce.domain.product.Product;
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
public class Member extends BaseTimeEntity implements UserDetails {

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

  @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Product> products;

  @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
  private Cart cart;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // Role 에서 접두사 "ROLE_" 이미 추가함
    return List.of(new SimpleGrantedAuthority(role.name()));
  }

  @Override
  public String getUsername() {
    return this.email; // 사용자명으로 이메일 사용
  }

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
