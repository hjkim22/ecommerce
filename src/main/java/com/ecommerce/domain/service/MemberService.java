package com.ecommerce.domain.service;

import com.ecommerce.domain.dto.member.MemberDto;
import com.ecommerce.domain.dto.member.MemberUpdateDto;
import com.ecommerce.domain.entity.MemberEntity;
import com.ecommerce.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

  private final MemberRepository memberRepository;

  // TODO
  public void signUp() {}

  public void signIn() {}

  public void 이메일관련메서드() {}

  // 조회 - id
  public MemberDto getMemberById(Long id) {
    MemberEntity member = memberRepository.findById(id).orElse(null);
    return MemberDto.fromEntity(member);
  }

  // 조회 - email
  public MemberDto getMemberByEmail(String email) {
    MemberEntity member = memberRepository.findByEmail(email).orElse(null);
    return MemberDto.fromEntity(member);
  }

  // 업데이트
  public MemberDto updateMember(Long id, MemberUpdateDto request) {
    MemberEntity member = memberRepository.findById(id).orElse(null);

    member.setEmail(request.getEmail());
    member.setName(request.getName());
    member.setPhoneNumber(request.getPhone());
    member.setAddress(request.getAddress());

    return MemberDto.fromEntity(memberRepository.save(member));
  }

  // 삭제
  public void deleteMember(Long id) {
    memberRepository.deleteById(id);
  }
}
