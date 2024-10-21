package com.ecommerce.domain.service;

import com.ecommerce.common.enums.ErrorCode;
import com.ecommerce.common.exception.CustomException;
import com.ecommerce.common.security.TokenProvider;
import com.ecommerce.domain.dto.member.MemberDto;
import com.ecommerce.domain.dto.member.MemberUpdateDto;
import com.ecommerce.domain.dto.member.SignInDto;
import com.ecommerce.domain.dto.member.SignUpDto;
import com.ecommerce.domain.dto.member.SignUpDto.Request;
import com.ecommerce.domain.entity.MemberEntity;
import com.ecommerce.domain.repository.MemberRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenProvider tokenProvider;

  /**
   * 회원가입
   * @param request 회원가입 요청 DTO
   * @return 회원가입 성공 응답 DTO
   */
  public SignUpDto.Response signUp(SignUpDto.Request request) {
    log.info("회원가입 요청: {}", request.getEmail());
    if (memberRepository.existsByEmail(request.getEmail())) {
      log.warn("회원가입-이미 존재하는 이메일: {}", request.getEmail());
      throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
    if (memberRepository.existsByPhoneNumber(request.getPhoneNumber())) {
      log.warn("회원가입-이미 존재하는 전화번호: {}", request.getPhoneNumber());
      throw new CustomException(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
    }
    if (request.getPassword().length() < 6) { // 비밀번호 길이 확인
      log.warn("6자리 미만의 비밀번호: {}", request.getPassword());
      throw new CustomException(ErrorCode.SHORT_PASSWORD);
    }

    String encodedPassword = passwordEncoder.encode(request.getPassword());
    MemberEntity savedMember = createMember(request, encodedPassword);

    return new SignUpDto.Response(savedMember.getEmail(), savedMember.getName(), "회원가입 완료");
  }

  /**
   * 로그인
   * @param request 로그인 요청 DTO
   * @return 로그인 성공 응답 DTO
   */
  public SignInDto.Response signIn(SignInDto.Request request) {
    log.info("로그인 시도: {}", request.getEmail());

    MemberEntity member = memberRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> {
          log.warn("로그인 실패 - 사용자 없음: {}", request.getEmail());
          return new CustomException(ErrorCode.USER_NOT_FOUND);
        });

    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      log.warn("로그인 실패 - 비밀번호 불일치: {}", request.getEmail());
      throw new CustomException(ErrorCode.INVALID_PASSWORD);
    }

    // 토큰 생성
    String token = tokenProvider.generateToken(member.getEmail(), member.getRole());
    log.info("로그인 성공: {}", member.getEmail());

    return new SignInDto.Response(token, member.getEmail(), "로그인 성공");
  }

  /**
   * 회원 정보 조회
   * @param id 회원 ID
   * @return 회원 정보 DTO
   */
  public MemberDto getMemberById(Long id) {
    MemberEntity member = memberRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    return MemberDto.fromEntity(member);
  }

  /**
   * 회원 정보 조회
   * @param email 회원 이메일
   * @return 회원 정보 DTO
   */
  public MemberDto getMemberByEmail(String email) {
    MemberEntity member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    return MemberDto.fromEntity(member);
  }

  /**
   * 회원 정보 업데이트
   * @param id      회원 ID
   * @param request 업데이트 요청 DTO
   * @return 업데이트된 회원 정보 DTO
   */
  public MemberDto updateMember(Long id, MemberUpdateDto request) {
    log.info("회원 정보 업데이트 요청 - ID: {}, 이메일: {}", id, request.getEmail());

    MemberEntity member = memberRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("업데이트 실패 - 사용자 없음: {}", id);
          return new CustomException(ErrorCode.USER_NOT_FOUND);
        });

    if (memberRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
      log.warn("이미 존재하는 이메일: {}", request.getEmail());
      throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    if (memberRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), id)) {
      log.warn("이미 존재하는 전화번호: {}", request.getPhoneNumber());
      throw new CustomException(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
    }

    member.setEmail(request.getEmail());
    member.setName(request.getName());
    member.setPhoneNumber(request.getPhoneNumber());
    member.setAddress(request.getAddress());

    log.info("회원 정보 업데이트 성공 - ID: {}", id);
    return MemberDto.fromEntity(memberRepository.save(member));
  }

  /**
   * 회원 삭제
   * @param id 회원 ID
   */
  public void deleteMember(Long id) {
    log.info("회원 삭제 요청 - ID: {}", id);

    if (!memberRepository.existsById(id)) {
      log.warn("삭제 실패 - 사용자 없음: {}", id);
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }
    memberRepository.deleteById(id);
    log.info("회원 삭제 성공 - ID: {}", id);
  }

  /**
   * 회원 엔티티 생성
   * @param request 회원가입 요청 DTO
   * @return 저장된 회원 엔티티
   */
  private MemberEntity createMember(Request request, String encodedPassword) {
    return memberRepository.save(MemberEntity.builder()
        .email(request.getEmail().toLowerCase(Locale.ROOT))
        .password(encodedPassword)
        .name(request.getName())
        .phoneNumber(request.getPhoneNumber())
        .address(request.getAddress())
        .role(request.getRole())
        .build()
    );
  }
}
