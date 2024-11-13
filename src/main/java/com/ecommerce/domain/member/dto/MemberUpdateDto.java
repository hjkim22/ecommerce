package com.ecommerce.domain.member.dto;

import jakarta.persistence.Column;
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

  @NotBlank(message = "이름은 필수입니다.")
  private String name;

  @NotBlank(message = "전화번호는 필수입니다.")
  @Column(unique = true)
  private String phoneNumber;

  @NotBlank(message = "주소는 필수입니다.")
  private String address;
}
