package com.ecommerce.domain.service;

import com.ecommerce.common.enums.ErrorCode;
import com.ecommerce.common.exception.CustomException;
import com.ecommerce.common.util.RedisUtil;
import com.ecommerce.domain.dto.email.EmailVerificationDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmailService {

  private final JavaMailSender mailSender; // 이메일 발송을 위한 JavaMailSender
  private final RedisUtil redisUtil;
  private static final long VERIFICATION_CODE_EXPIRATION_TIME = 180L;

  /**
   * 인증 코드 생성
   * @return 6자리 인증 코드
   */
  public String generateVerificationCode() {
    Random random = new Random();
    int verificationCode = random.nextInt(1000000);
    String formattedCode = String.format("%06d", verificationCode); // 6자리로 포맷
    log.info("생성된 인증 번호: {}", formattedCode);
    return formattedCode;
  }

  /**
   * 인증 메일 전송
   *
   * @param email 이메일 주소
   */
  public void sendEmailVerification(String email) {
    String verificationCode = generateVerificationCode();
    String title = "회원가입 인증 메일";
    String content = "<html>"
        + "<body>"
        + "<h1>이메일 인증 코드: " + verificationCode + "</h1>" // 인증 코드 포함 HTML 내용
        + "<p>해당 코드를 입력하세요.</p>"
        + "</body>"
        + "</html>";

    sendHtmlEmail(email, title, content);
    cacheVerificationCode(verificationCode, email);
  }

  /**
   * HTML 형식 이메일 전송
   * @param toEmail 이메일 주소
   * @param title 제목
   * @param content 내용
   */
  public void sendHtmlEmail(String toEmail, String title, String content) {
    MimeMessage message = mailSender.createMimeMessage();

    try {
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setTo(toEmail);
      helper.setSubject(title);
      helper.setText(content, true); // 내용을 HTML 로 설정
      mailSender.send(message);
    } catch (MessagingException e) {
      log.error("이메일 전송 실패: {}", e.getMessage());
      throw new RuntimeException("이메일 전송 실패", e);
    }
  }

  /**
   * 인증 코드 Redis 에 캐시
   * @param verificationCode 인증 코드
   * @param email 이메일 주소
   */
  private void cacheVerificationCode(String verificationCode, String email) {
    String cacheKey = "email_verification:" + verificationCode; // Redis 키 형식 통일
    redisUtil.setData(cacheKey, email, VERIFICATION_CODE_EXPIRATION_TIME); // 3분 후 만료
  }

  /**
   * 인증 코드 검증
   * @param emailVerificationDto 인증 코드 / 이메일 주소 포함 DTO
   */
  public void validateVerificationCode(EmailVerificationDto emailVerificationDto) {
    String cacheKey = "email_verification:" + emailVerificationDto.getVerificationCode();
    String cachedEmail = redisUtil.getData(cacheKey);

    if (cachedEmail == null) {
      log.warn("인증 코드 만료: {}", emailVerificationDto.getVerificationCode());
      throw new CustomException(ErrorCode.VERIFICATION_CODE_EXPIRED);
    }

    if (!emailVerificationDto.getEmail().equals(cachedEmail)) {
      log.warn("잘못된 인증 코드: {}", emailVerificationDto.getVerificationCode());
      throw new CustomException(ErrorCode.INVALID_VERIFICATION_CODE);
    }

    log.info("인증 코드 검증 성공");
  }
}
