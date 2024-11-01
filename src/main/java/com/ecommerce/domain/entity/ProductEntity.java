package com.ecommerce.domain.entity;

import com.ecommerce.common.enums.ProductStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String productName;
  private String description;
  private BigDecimal price;
  private Integer stockQuantity;

  @ManyToOne(fetch = FetchType.LAZY) // 지연로딩, 필드에 접근할 때만 필요한 정보 가져옴
  @JoinColumn(name = "member_id", nullable = false)
  private MemberEntity seller;

  @Enumerated(EnumType.STRING)
  private ProductStatus status;
}
