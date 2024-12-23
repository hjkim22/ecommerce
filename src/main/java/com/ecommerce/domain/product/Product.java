package com.ecommerce.domain.product;

import com.ecommerce.common.entity.BaseTimeEntity;
import com.ecommerce.common.enums.ProductStatus;
import com.ecommerce.domain.member.Member;
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
public class Product extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String productName;
  private String description;
  private BigDecimal price;
  private Integer stockQuantity;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member seller;

  @Enumerated(EnumType.STRING)
  private ProductStatus status;

  public void setStockQuantity(Integer stockQuantity) {
    this.stockQuantity = stockQuantity;

    if (stockQuantity == 0) {
      this.status = ProductStatus.OUT_OF_STOCK; // 품절 상태로 변경
    } else if (stockQuantity > 0) {
      this.status = ProductStatus.AVAILABLE;
    }
  }
}
