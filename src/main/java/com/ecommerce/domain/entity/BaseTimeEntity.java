package com.ecommerce.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseTimeEntity {

  @CreatedDate              // entity 생성 시, 자동으로 시간 기록
  @Column(nullable = false) // 생성될때만 실행되고, 이 후 변경되지 않음
  private LocalDateTime createdAt;

  @LastModifiedDate         // entity 수정 시, 자동으로 시간 기록
  private LocalDateTime updatedAt;
}
