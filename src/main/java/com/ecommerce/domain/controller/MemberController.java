package com.ecommerce.domain.controller;

import static org.springframework.http.HttpStatus.CREATED;

import com.ecommerce.domain.dto.email.EmailVerificationDto;
import com.ecommerce.domain.dto.email.EmailVerificationRequestDto;
import com.ecommerce.domain.dto.member.MemberDto;
import com.ecommerce.domain.dto.member.MemberUpdateDto;
import com.ecommerce.domain.dto.member.SignInDto;
import com.ecommerce.domain.dto.member.SignUpDto;
import com.ecommerce.domain.service.EmailService;
import com.ecommerce.domain.service.MemberService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/members")
public class MemberController {

  private final MemberService memberService;
  private final EmailService emailService;

  // 회원가입
  @PostMapping("/sign-up")
  public ResponseEntity<SignUpDto.Response> signUp(@Valid @RequestBody SignUpDto.Request request) {
    log.info("회원가입 요청 - 이메일: {}", request.getEmail());
    SignUpDto.Response newMember = memberService.signUp(request);
    log.info("회원가입 성공 - 이메일: {}", newMember.getEmail());
    return ResponseEntity.status(CREATED).body(newMember);
  }

  // 로그인
  @PostMapping("/sign-in")
  public ResponseEntity<SignInDto.Response> signIn(@Valid @RequestBody SignInDto.Request request) {
    log.info("로그인 요청 - 이메일: {}", request.getEmail());
    SignInDto.Response newMember = memberService.signIn(request); // 로그인 서비스에서 토큰을 포함한 응답 받음
    log.info("로그인 성공 - 이메일: {}", newMember.getEmail());
    return ResponseEntity.ok(newMember); // 로그인 성공 시 JWT 토큰과 이메일을 포함한 응답 반환
  }

  // 인증 메일 전송
  @PostMapping("/verification-code/send")
  public ResponseEntity<Void> sendEmail(@RequestBody @Valid EmailVerificationRequestDto request)
      throws MessagingException {
    log.info("인증 메일 전송 요청 - 이메일: {}", request.email());
    emailService.sendEmailVerification(request.email());
    log.info("인증 메일 전송 완료");
    return ResponseEntity.noContent().build();
  }

  // 인증번호 확인
  @PostMapping("/verification-code/verify")
  public ResponseEntity<Void> verifyEmailCode(@RequestBody @Valid EmailVerificationDto request) {
    log.info("인증번호 확인 요청 - 인증 코드: {}", request.getVerificationCode());
    emailService.validateVerificationCode(request);
    log.info("인증번호 확인 성공");
    return ResponseEntity.ok().build();
  }

  // 회원조회 id
  @GetMapping("/{memberId}")
  public ResponseEntity<MemberDto> getMemberById(@PathVariable("memberId") Long memberId) {
    log.info("회원 정보 조회 요청 - ID: {}", memberId);
    return ResponseEntity.ok(memberService.getMemberById(memberId));
  }

  // 회원조회 email
  @GetMapping("/email/{email}") // id 와 url 충돌방지
  public ResponseEntity<MemberDto> getMemberByEmail(@PathVariable("email") String email) {
    log.info("회원 정보 조회 요청 - 이메일: {}", email);
    return ResponseEntity.ok(memberService.getMemberByEmail(email));
  }

  // 최근 가입된 회원 조회
  @GetMapping("/recent")
  public ResponseEntity<List<MemberDto>> getRecentMembers(
      @RequestParam(defaultValue = "10") int limit) {
    log.info("최근 가입된 회원 조회 요청 - {}", limit);
    List<MemberDto> recentMembers = memberService.findRecentMembers(limit);
    return ResponseEntity.ok(recentMembers);
  }

  // 업데이트
  @PutMapping("/{memberId}")
  public ResponseEntity<MemberDto> updateMember(
      @PathVariable("memberId") Long memberId,
      @Valid @RequestBody MemberUpdateDto request) {
    log.info("회원 정보 업데이트 요청 - ID: {}", memberId);
    return ResponseEntity.ok(memberService.updateMember(memberId, request));
  }

  // 삭제
  @DeleteMapping("/{memberId}")
  public ResponseEntity<Void> deleteMember(@PathVariable Long memberId) {
    log.info("회원 삭제 요청 - ID: {}", memberId);
    memberService.deleteMember(memberId);
    return ResponseEntity.noContent().build();
  }
}
