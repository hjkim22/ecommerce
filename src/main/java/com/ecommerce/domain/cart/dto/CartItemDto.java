package com.ecommerce.domain.cart.dto;

import com.ecommerce.domain.cart.CartItem;
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

  public static CartItemDto fromEntity(CartItem cartItemEntity) {
    return CartItemDto.builder()
        .cartItemId(cartItemEntity.getId())
        .productId(cartItemEntity.getProduct().getId())
        .quantity(cartItemEntity.getQuantity())
        .productPrice(cartItemEntity.getProduct().getPrice())
        .build();
  }
}
