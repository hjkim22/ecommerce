package com.ecommerce.domain.dto.cartItem;

import com.ecommerce.domain.entity.CartItemEntity;
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
public class CartItemDto {

  private Long cartItemId;
  private Long productId;
  private Integer quantity;
  private BigDecimal productPrice;

  public static CartItemDto fromEntity(CartItemEntity cartItemEntity) {
    return CartItemDto.builder()
        .cartItemId(cartItemEntity.getId())
        .productId(cartItemEntity.getProduct().getId())
        .quantity(cartItemEntity.getQuantity())
        .productPrice(cartItemEntity.getProduct().getPrice())
        .build();
  }
}
