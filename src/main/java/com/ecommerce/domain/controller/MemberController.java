package com.ecommerce.domain.controller;

import static org.springframework.http.HttpStatus.CREATED;

import com.ecommerce.domain.dto.member.MemberDto;
import com.ecommerce.domain.dto.member.MemberUpdateDto;
import com.ecommerce.domain.dto.member.SignInDto;
import com.ecommerce.domain.dto.member.SignUpDto;
import com.ecommerce.domain.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/members")
public class MemberController {

  private final MemberService memberService;

  // 회원가입
  @PostMapping("/sign-up")
  public ResponseEntity<SignUpDto.Response> signUp(@Valid @RequestBody SignUpDto.Request request) {
    log.info("Sign-up request for email: {}", request.getEmail());
    SignUpDto.Response newMember = memberService.signUp(request);
    log.info("Sign-up successful for email: {}", newMember.getEmail());
    return ResponseEntity.status(CREATED).body(newMember);
  }

  // 로그인
  @PostMapping("/sign-in")
  public ResponseEntity<SignInDto.Response> signIn(@Valid @RequestBody SignInDto.Request request) {
    log.info("Sign-in request for email: {}", request.getEmail());
    SignInDto.Response newMember = memberService.signIn(request); // 로그인 서비스에서 토큰을 포함한 응답 받음
    log.info("Sign-in successful for email: {}", newMember.getEmail());
    return ResponseEntity.ok(newMember); // 로그인 성공 시 JWT 토큰과 이메일을 포함한 응답 반환
  }

  // 회원조회 id
  @GetMapping("/{memberId}")
  public ResponseEntity<MemberDto> getMemberById(@PathVariable("memberId") Long memberId) {
    log.info("Fetching member by ID: {}", memberId);
    return ResponseEntity.ok(memberService.getMemberById(memberId));
  }

  // 회원조회 email
  @GetMapping("/email/{email}") // id 와 url 충돌방지
  public ResponseEntity<MemberDto> getMemberByEmail(@PathVariable("email") String email) {
    log.info("Fetching member by email: {}", email);
    return ResponseEntity.ok(memberService.getMemberByEmail(email));
  }

  // 업데이트
  @PutMapping("/{memberId}")
  public ResponseEntity<MemberDto> updateMember(
      @PathVariable("memberId") Long memberId,
      @Valid @RequestBody MemberUpdateDto request) {

    log.info("Updating member with ID: {}", memberId);
    return ResponseEntity.ok(memberService.updateMember(memberId, request));
  }

  // 삭제
  @DeleteMapping("/{memberId}")
  public ResponseEntity<Void> deleteMember(@PathVariable Long memberId) {
    log.info("Deleting member with ID: {}", memberId);

    memberService.deleteMember(memberId);
    return ResponseEntity.noContent().build();
  }
}
