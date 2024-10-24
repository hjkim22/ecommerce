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

  @NotBlank(message = "이메일을 입력해주세요.")
  @Email(message = "유효한 이메일 주소를 입력해주세요.")
  private String email;

  @NotBlank(message = "이름을 입력해주세요..")
  private String name;

  @NotBlank(message = "전화번호를 입력해주세요.")
  @Column(unique = true)
  private String phoneNumber;

  @NotBlank(message = "주소를 입력해주세요.")
  private String address;
}
