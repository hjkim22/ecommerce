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
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  @Transactional
  public SignUpDto.Response signUp(SignUpDto.Request request) {
    if (memberRepository.existsByEmail(request.getEmail())) {
      throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
    if (memberRepository.existsByPhoneNumber(request.getPhoneNumber())) {
      throw new CustomException(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
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
    MemberEntity member = memberRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new CustomException(ErrorCode.INVALID_PASSWORD);
    }

    // 토큰 생성
    String token = tokenProvider.generateToken(member.getEmail(), member.getRole());

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
   * 최근 가입한 회원 조회
   * @param limit 조회할 회원 수
   * @return 최근 가입한 회원 정보 리스트 DTO
   */
  public List<MemberDto> findRecentMembers(int limit) {
    final int MAX_LIMIT = 50;
    if (limit > MAX_LIMIT) {
      log.warn("요청한 limit {}는 최대 {}를 초과하여 기본값 10으로 설정합니다.", limit, MAX_LIMIT);
      limit = 10; // 기본값으로 대체
    }

    Pageable pageable = PageRequest.of(0, limit);
    List<MemberEntity> recentMembers = memberRepository.findAllByOrderByCreatedAtDesc(pageable);
    log.info("최근 가입한 회원 {}명 조회", recentMembers.size());
    return recentMembers.stream()
        .map(MemberDto::fromEntity)
        .toList();
  }

  /**
   * 회원 정보 업데이트
   * @param id      회원 ID
   * @param request 업데이트 요청 DTO
   * @return 업데이트된 회원 정보 DTO
   */
  @Transactional
  public MemberDto updateMember(Long id, MemberUpdateDto request) {
    MemberEntity member = memberRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    if (memberRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
      throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    if (memberRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), id)) {
      throw new CustomException(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
    }

    member.setEmail(request.getEmail());
    member.setName(request.getName());
    member.setPhoneNumber(request.getPhoneNumber());
    member.setAddress(request.getAddress());

    log.info("회원 정보 업데이트 성공 - ID: {}", id);
    return MemberDto.fromEntity(member);
  }

  /**
   * 회원 삭제
   * @param id 회원 ID
   */
  @Transactional
  public void deleteMember(Long id) {
    if (!memberRepository.existsById(id)) {
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
