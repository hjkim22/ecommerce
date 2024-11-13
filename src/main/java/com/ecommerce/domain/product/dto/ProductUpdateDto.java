package com.ecommerce.domain.product.dto;

import com.ecommerce.common.enums.ProductStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateDto {

  private String productName;
  private String description;

  @DecimalMin(value = "0.0", inclusive = false, message = "가격은 0보다 커야 합니다.")
  private BigDecimal price;

  @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
  private Integer stockQuantity;

  @Enumerated(EnumType.STRING)
  private ProductStatus status;
}
