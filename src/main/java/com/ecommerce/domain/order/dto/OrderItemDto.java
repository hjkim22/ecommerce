package com.ecommerce.domain.order.dto;

import com.ecommerce.domain.order.OrderItemEntity;
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
public class OrderItemDto {

  private Long orderItemId;
  private Long productId;
  private Integer quantity;
  private BigDecimal price;

  public static OrderItemDto fromEntity(OrderItemEntity orderItemEntity) {
    return OrderItemDto.builder()
        .orderItemId(orderItemEntity.getId())
        .productId(orderItemEntity.getProduct().getId())
        .quantity(orderItemEntity.getQuantity())
        .price(orderItemEntity.getPrice())
        .build();
  }
}
