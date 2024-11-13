package com.ecommerce.domain.member;

import com.ecommerce.common.enums.ErrorCode;
import com.ecommerce.common.enums.Role;
import com.ecommerce.common.exception.CustomException;
import com.ecommerce.common.repository.RedisCacheRepository;
import com.ecommerce.common.security.TokenProvider;
import com.ecommerce.domain.cart.CartService;
import com.ecommerce.domain.member.dto.MemberDto;
import com.ecommerce.domain.member.dto.MemberUpdateDto;
import com.ecommerce.domain.member.dto.SignInDto;
import com.ecommerce.domain.member.dto.SignUpDto;
import com.ecommerce.domain.member.dto.SignUpDto.Request;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
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
  private final CartService cartService;
  private final RedisCacheRepository redisCacheRepository;

  /**
   * 회원가입
   *
   * @param request 회원가입 요청 DTO
   * @return 회원가입 응답 DTO
   */
  @Transactional
  public SignUpDto.Response signUp(SignUpDto.Request request) {
    validateMemberExists(request.getEmail(), request.getPhoneNumber());

    String encodedPassword = passwordEncoder.encode(request.getPassword());
    Member savedMember = createMember(request, encodedPassword);

    // CUSTOMER 인 경우만 장바구니 생성
    if (savedMember.getRole() == Role.CUSTOMER) {
      cartService.createCartForMember(savedMember);
    }

    return new SignUpDto.Response(savedMember.getEmail(), savedMember.getName(), "회원가입 완료");
  }

  /**
   * 로그인
   *
   * @param request 로그인 요청 DTO
   * @return 로그인 성공 DTO
   */
  public SignInDto.Response signIn(SignInDto.Request request) {
    Member member = findMemberByEmail(request.getEmail());

    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new CustomException(ErrorCode.INVALID_PASSWORD);
    }

    // 토큰 생성
    String token = tokenProvider.createToken(member.getId(), member.getRole());
    redisCacheRepository.setData("user:session:" + member.getId(), token, 3600L);

    return new SignInDto.Response(token, member.getEmail(), "로그인 성공");
  }

  /**
   * 로그아웃
   *
   * @param userId 로그아웃할 사용자 ID
   */
  public void logout(Long userId) {
    redisCacheRepository.deleteData("user:session:" + userId); // Redis 에서 세션 삭제
  }

  /**
   * 회원 정보 조회
   *
   * @param memberId 회원 ID
   * @return 회원 정보 DTO
   */
  public MemberDto getMemberById(Long memberId) {
    Member member = findMemberById(memberId);
    return MemberDto.fromEntity(member);
  }

  /**
   * 회원 정보 조회
   *
   * @param email 회원 이메일
   * @return 회원 정보 DTO
   */
  public MemberDto getMemberByEmail(String email) {
    Member member = findMemberByEmail(email);
    return MemberDto.fromEntity(member);
  }

  /**
   * 회원 리스트 조회
   *
   * @param role     회원 역할 (SELLER, CUSTOMER)
   * @param pageable 페이징 정보
   * @return 역할별 회원 목록
   */
  public Page<MemberDto> getMembersByRole(Role role, Pageable pageable) {
    Page<Member> members = memberRepository.findMemberByRole(role, pageable);
    return members.map(MemberDto::fromEntity);
  }

  /**
   * 전체 회원 리스트 조회
   *
   * @param pageable 페이징 정보
   * @return 전체 회원 목록 - 최신순
   */
  public Page<MemberDto> getAllMembers(Pageable pageable) {
    Pageable sortedByCreatedAtDesc = PageRequest.of(
        pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.desc("createdAt")));
    Page<Member> members = memberRepository.findAll(sortedByCreatedAtDesc);

    return members.map(MemberDto::fromEntity);
  }

  /**
   * 회원 정보 업데이트
   *
   * @param targetMemberId  회원 ID
   * @param request         업데이트 요청 DTO
   * @param requestMemberId 요청한 회원 ID (권한 확인용)
   * @return 업데이트된 회원 정보 DTO
   */
  @Transactional
  public MemberDto updateMember(Long targetMemberId, MemberUpdateDto request,
      Long requestMemberId) {
    Member member = findMemberById(targetMemberId);
    validateOwnership(requestMemberId, targetMemberId);

    if (memberRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), targetMemberId)) {
      throw new CustomException(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
    }

    member.setName(request.getName());
    member.setPhoneNumber(request.getPhoneNumber());
    member.setAddress(request.getAddress());

    log.info("회원 정보 업데이트 성공 - ID: {}", targetMemberId);
    return MemberDto.fromEntity(member);
  }

  /**
   * 회원 삭제
   *
   * @param targetMemberId  회원 ID
   * @param requestMemberId 요청한 회원 ID (권한 확인용)
   */
  @Transactional
  public void deleteMember(Long targetMemberId, Long requestMemberId) {
    findMemberById(targetMemberId);
    validateOwnership(requestMemberId, targetMemberId);

    memberRepository.deleteById(targetMemberId);
    log.info("회원 삭제 성공 - ID: {}", targetMemberId);
  }

  // ================================= Helper methods ================================= //

  // 회원 중복 체크 메서드
  private void validateMemberExists(String email, String phoneNumber) {
    if (memberRepository.existsByEmail(email)) {
      throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
    if (memberRepository.existsByPhoneNumber(phoneNumber)) {
      throw new CustomException(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
    }
  }

  // 이메일로 회원 조회
  private Member findMemberByEmail(String email) {
    return memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
  }

  // ID로 회원 조회
  private Member findMemberById(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
  }

  // 소유자 권한 확인
  private void validateOwnership(Long requestMemberId, Long memberId) {
    if (!isAdmin() && !memberId.equals(requestMemberId)) {
      throw new CustomException(ErrorCode.INVALID_AUTH_TOKEN);
    }
  }

  // 어드민 권한 확인
  private boolean isAdmin() {
    return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + Role.ADMIN.name()));
  }

  // 회원 엔티티 생성
  private Member createMember(Request request, String encodedPassword) {
    return memberRepository.save(Member.builder()
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
