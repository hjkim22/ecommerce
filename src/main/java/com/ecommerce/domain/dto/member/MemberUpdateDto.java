package com.ecommerce.domain.dto.member;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateDto {

  @NotBlank
  @Email
  @Column(unique = true)
  private String email;

  @NotBlank
  private String name;

  @NotBlank
  @Column(unique = true)
  private String phone;

  @NotBlank
  private String address;
}
