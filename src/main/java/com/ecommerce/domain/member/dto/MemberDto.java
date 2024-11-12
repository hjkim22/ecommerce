package com.ecommerce.domain.member.dto;

import com.ecommerce.common.enums.Role;
import com.ecommerce.domain.member.MemberEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberDto {

  // 조회용 DTO 라서 유효성 검증 필요 없음.
  private Long id;
  private String email;
  private String name;
  private String phoneNumber;
  private String address;
  private Role role;

  public static MemberDto fromEntity(MemberEntity memberEntity) {
    return MemberDto.builder()
        .id(memberEntity.getId())
        .email(memberEntity.getEmail())
        .name(memberEntity.getName())
        .phoneNumber(memberEntity.getPhoneNumber())
        .address(memberEntity.getAddress())
        .role(memberEntity.getRole())
        .build();
  }
}
