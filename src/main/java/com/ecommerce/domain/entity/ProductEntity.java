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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

  @NotBlank
  private String productName;

  @NotBlank
  private String description;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = false)
  private BigDecimal price;

  @NotNull
  @Min(0)
  private int stockQuantity;

  @ManyToOne(fetch = FetchType.LAZY) // 지연로딩, 필드에 접근할 때만 필요한 정보 가져옴
  @JoinColumn(name = "member_id", nullable = false)
  private MemberEntity seller;

  @Enumerated(EnumType.STRING)
  private ProductStatus status = ProductStatus.AVAILABLE;
}
