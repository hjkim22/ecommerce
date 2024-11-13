package com.ecommerce.domain.product.dto;

import com.ecommerce.common.enums.ProductStatus;
import com.ecommerce.domain.product.ProductEntity;
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
public class ProductDto {

  private Long id;
  private String productName;
  private String description;
  private BigDecimal price;
  private int stockQuantity;
  private Long sellerId;
  private ProductStatus status;

  public static ProductDto fromEntity(ProductEntity productEntity) {
    return ProductDto.builder()
        .id(productEntity.getId())
        .productName(productEntity.getProductName())
        .description(productEntity.getDescription())
        .price(productEntity.getPrice())
        .stockQuantity(productEntity.getStockQuantity())
        .sellerId(productEntity.getSeller().getId())
        .status(productEntity.getStatus())
        .build();
  }
}
